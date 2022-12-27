package com.tmorgner.gradle.processors;

import com.tmorgner.gradle.model.scan.DependencyClassData;
import com.tmorgner.gradle.model.scan.DependencyTargetRecord;
import com.tmorgner.gradle.model.scan.DependencyUsageType;
import com.tmorgner.gradle.model.scan.DependencyUsageLocationRecord;
import org.objectweb.asm.*;

public class FieldProcessor extends FieldVisitor {
  private DependencyUsageLocationRecord location;
  private final DependencyClassData classData;
  private final AnnotationProcessor annotationProcessor;

  public FieldProcessor(final DependencyClassData classData) {
    super(Opcodes.ASM9);
    this.classData = classData;
    this.annotationProcessor = new AnnotationProcessor(classData);
  }

  public void init(DependencyUsageLocationRecord location) {
    this.location = location;
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String descriptor,
                                           final boolean visible) {
    Type t = Type.getType(descriptor);
    classData.record(location, new DependencyTargetRecord(t.getClassName(), ".class", DependencyUsageType.Attribute));

    annotationProcessor.init(location.withType(DependencyUsageType.Attribute));
    return annotationProcessor;
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(final int typeRef,
                                               final TypePath typePath,
                                               final String descriptor,
                                               final boolean visible) {
    return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
  }
}
