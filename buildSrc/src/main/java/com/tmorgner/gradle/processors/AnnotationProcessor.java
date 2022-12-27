package com.tmorgner.gradle.processors;

import com.tmorgner.gradle.actions.AsmUtils;
import com.tmorgner.gradle.model.scan.DependencyClassData;
import com.tmorgner.gradle.model.scan.DependencyTargetRecord;
import com.tmorgner.gradle.model.scan.DependencyUsageType;
import com.tmorgner.gradle.model.scan.DependencyUsageLocationRecord;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class AnnotationProcessor extends AnnotationVisitor {
  private final DependencyClassData classData;
  private DependencyUsageLocationRecord record;

  public AnnotationProcessor(DependencyClassData classData) {
    super(Opcodes.ASM9);
    this.classData = classData;
  }

  public void init(DependencyUsageLocationRecord record) {
    this.record = record;
  }

  @Override
  public void visit(final String name, final Object value) {
    super.visit(name, value);
    if (value instanceof Type) {
      Type t = (Type) value;
      this.classData.record(record, new DependencyTargetRecord(AsmUtils.getBaseName(t), ".class", DependencyUsageType.TypeReference));
    }
  }
}
