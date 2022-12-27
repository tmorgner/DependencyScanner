package com.tmorgner.gradle.processors;

import com.tmorgner.gradle.model.scan.DependencyClassData;
import com.tmorgner.gradle.model.scan.DependencyTargetRecord;
import com.tmorgner.gradle.model.scan.DependencyUsageType;
import com.tmorgner.gradle.model.scan.DependencyUsageLocationRecord;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureVisitor;

public class SignatureProcessor extends SignatureVisitor {
  private final DependencyClassData classData;
  private DependencyUsageLocationRecord location;

  public SignatureProcessor(final DependencyClassData classData) {
    super(Opcodes.ASM9);
    this.classData = classData;
  }

  public void init(DependencyUsageLocationRecord location) {
    this.location = location;
  }

  @Override
  public void visitClassType(final String name) {
    super.visitClassType(name);
    classData.record(location, new DependencyTargetRecord(name, ".class", DependencyUsageType.TypeReference));
  }

  @Override
  public void visitInnerClassType(final String name) {
    super.visitInnerClassType(name);
    classData.record(location, new DependencyTargetRecord(name, ".class", DependencyUsageType.TypeReference));
  }
}
