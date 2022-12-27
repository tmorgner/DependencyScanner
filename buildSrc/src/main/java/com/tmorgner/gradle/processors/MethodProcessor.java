package com.tmorgner.gradle.processors;

import com.tmorgner.gradle.actions.AsmUtils;
import com.tmorgner.gradle.model.scan.DependencyClassData;
import com.tmorgner.gradle.model.scan.DependencyTargetRecord;
import com.tmorgner.gradle.model.scan.DependencyUsageType;
import com.tmorgner.gradle.model.scan.DependencyUsageLocationRecord;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;

public class MethodProcessor extends MethodVisitor {
  private DependencyUsageLocationRecord location;
  private final DependencyClassData classData;
  private final AnnotationProcessor annotationProcessor;
  private final SignatureProcessor signatureProcessor;

  public MethodProcessor(final DependencyClassData classData) {
    super(Opcodes.ASM9);
    this.classData = classData;
    this.signatureProcessor = new SignatureProcessor(classData);
    this.annotationProcessor = new AnnotationProcessor(classData);
  }

  public void init(DependencyUsageLocationRecord location) {
    this.location = location;
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
    Type t = Type.getType(descriptor);
    classData.record(location, new DependencyTargetRecord(t.getClassName(), ".class", DependencyUsageType.Attribute));

    annotationProcessor.init(location.withType(DependencyUsageType.Attribute));
    return annotationProcessor;
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
    return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
  }

  @Override
  public AnnotationVisitor visitParameterAnnotation(final int parameter, final String descriptor, final boolean visible) {
    return super.visitParameterAnnotation(parameter, descriptor, visible);
  }

  @Override
  public void visitTypeInsn(final int opcode, final String type) {
    Type t = Type.getObjectType(type);
    classData.record(location, new DependencyTargetRecord(AsmUtils.getBaseName(t), ".class", DependencyUsageType.Method));
  }

  @Override
  public void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
    Type t = Type.getType(descriptor);
    classData.record(location, new DependencyTargetRecord(AsmUtils.getBaseName(t), name, DependencyUsageType.Field));
  }

  @Override
  public void visitTryCatchBlock(final Label start, final Label end, final Label handler, final String type) {

    if (type == null) {
      return;
    }

    classData.record(location, new DependencyTargetRecord(type.replace("/", "."), ".class", DependencyUsageType.TypeReference));
  }

  @Override
  public AnnotationVisitor visitTryCatchAnnotation(final int typeRef, final TypePath typePath, final String descriptor, final boolean visible) {
    return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
  }

  @Override
  public void visitLocalVariable(final String name, final String descriptor, final String signature, final Label start, final Label end, final int index) {
    Type t = Type.getType(descriptor);
    if (t.getSort() == Type.ARRAY ||
        t.getSort() == Type.OBJECT) {
      classData.record(location, new DependencyTargetRecord(AsmUtils.getBaseName(t), ".class", DependencyUsageType.TypeReference));
    }

    if (signature != null) {
      final SignatureReader signatureReader = new SignatureReader(signature);
      signatureProcessor.init(location);
      signatureReader.accept(signatureProcessor);
    }
  }

  @Override
  public AnnotationVisitor visitLocalVariableAnnotation(final int typeRef, final TypePath typePath, final Label[] start, final Label[] end, final int[] index, final String descriptor, final boolean visible) {
    return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, descriptor, visible);
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    location = location.withLineNumber(line);
  }
}
