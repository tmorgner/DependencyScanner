package com.tmorgner.gradle.processors;

import com.tmorgner.gradle.actions.AsmUtils;
import com.tmorgner.gradle.model.scan.DependencyClassData;
import com.tmorgner.gradle.model.scan.DependencyTargetRecord;
import com.tmorgner.gradle.model.scan.DependencyUsageType;
import com.tmorgner.gradle.model.scan.DependencyUsageLocationRecord;
import org.objectweb.asm.*;
import org.objectweb.asm.signature.SignatureReader;

public class ClassProcessor extends ClassVisitor {
  private final DependencyClassData classData;
  private final SignatureProcessor signatureProcessor;
  private final MethodProcessor methodProcessor;
  private final AnnotationProcessor annotationVisitor;
  private final FieldProcessor fieldProcessor;
  private final boolean detailedScan;
  private final DependencyUsageLocationRecord currentLocation;

  public ClassProcessor(final DependencyClassData classData,
                        boolean detailedScan) {
    super(Opcodes.ASM9);
    this.classData = classData;
    this.annotationVisitor = new AnnotationProcessor(classData);
    this.signatureProcessor = new SignatureProcessor(classData);
    this.methodProcessor = new MethodProcessor(classData);
    this.fieldProcessor = new FieldProcessor(classData);
    this.detailedScan = detailedScan;
    currentLocation = new DependencyUsageLocationRecord(-1, ".class", DependencyUsageType.Class);
  }

  @Override
  public void visitSource(final String source, final String debug) {
    classData.setSourceFile(source);
  }

  public DependencyUsageLocationRecord currentLocation() {
    return currentLocation;
  }

  @Override
  public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces) {
    if (superName != null && !"java/lang/Object".equals(superName)) {
      classData.record(currentLocation(), new DependencyTargetRecord(superName, ".class", DependencyUsageType.TypeReference));
    }

    for (final String interfaceName : interfaces) {
      classData.record(currentLocation(), new DependencyTargetRecord(interfaceName, ".class", DependencyUsageType.TypeReference));
    }

    if (signature != null) {
      SignatureReader r = new SignatureReader(signature);
      signatureProcessor.init(currentLocation());
      r.accept(signatureProcessor);
    }
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
    if (!detailedScan) return null;

    Type t = Type.getType(desc);
    classData.record(currentLocation, new DependencyTargetRecord(t.getClassName(), ".class", DependencyUsageType.Attribute));
    final DependencyUsageLocationRecord location = currentLocation().withType(DependencyUsageType.Attribute);
    annotationVisitor.init(location);
    return annotationVisitor;
  }

  @Override
  public FieldVisitor visitField(final int access, final String name, final String desc, final String signature, final Object value) {

    DependencyUsageLocationRecord r = currentLocation().withField(name);
    classData.recordField(r.memberName);

    if (!detailedScan) return null;

    Type fieldType = Type.getType(desc);
    if (fieldType.getSort() == Type.ARRAY ||
        fieldType.getSort() == Type.OBJECT) {
      classData.record(r, new DependencyTargetRecord(AsmUtils.getBaseName(fieldType), ".class", DependencyUsageType.TypeReference));
    }

    if (signature != null) {
      signatureProcessor.init(r);
      final SignatureReader signatureReader = new SignatureReader(signature);
      signatureReader.accept(signatureProcessor);
    }
    fieldProcessor.init(r);
    return fieldProcessor;
  }

  @Override
  public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature, final String[] exceptions) {
    final Type ret = Type.getReturnType(desc);
    final Type[] argumentTypes = Type.getArgumentTypes(desc);
    DependencyUsageLocationRecord r = currentLocation().withMethod(AsmUtils.getMethodName(name, argumentTypes));

    classData.recordMethod(r.memberName);

    if (!detailedScan) return null;

    if (ret.getSort() == Type.ARRAY ||
        ret.getSort() == Type.OBJECT) {
      classData.record(r, new DependencyTargetRecord(AsmUtils.getBaseName(ret), ".class", DependencyUsageType.TypeReference));
    }

    for (final Type arg : argumentTypes) {
      if (arg.getSort() == Type.ARRAY ||
          arg.getSort() == Type.OBJECT) {
        classData.record(r, new DependencyTargetRecord(AsmUtils.getBaseName(arg), ".class", DependencyUsageType.TypeReference));
      }
    }

    if (signature != null) {
      signatureProcessor.init(r);
      final SignatureReader signatureReader = new SignatureReader(signature);
      signatureReader.accept(signatureProcessor);
    }

    methodProcessor.init(r);
    return methodProcessor;
  }
}

