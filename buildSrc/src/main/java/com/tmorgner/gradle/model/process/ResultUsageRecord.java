package com.tmorgner.gradle.model.process;

import com.tmorgner.gradle.model.scan.DependencyUsageType;

public class ResultUsageRecord {
  private final ResultMember member;
  private final DependencyUsageType type;
  private final String module;
  private final String packageName;
  private final String className;
  private final String memberName;
  private final int lineNumber;

  public ResultUsageRecord(final ResultMember member,
                           final DependencyUsageType type,
                           final String module,
                           final String packageName,
                           final String className,
                           final String memberName) {
    this(member, type, module, packageName, className, memberName, -1);
  }

  public ResultUsageRecord(final ResultMember member,
                           final DependencyUsageType type,
                           final String module,
                           final String packageName,
                           final String className,
                           final String memberName,
                           final int lineNumber) {
    this.member = member;
    this.type = type;
    this.module = module;
    this.packageName = packageName;
    this.className = className;
    this.memberName = memberName;
    this.lineNumber = lineNumber;
  }

  public String getModule() {
    return module;
  }

  public DependencyUsageType getType() {
    return type;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getClassName() {
    return className;
  }

  public String getMemberName() {
    return memberName;
  }

  public ResultMember getSourceMember() {
    return member;
  }

  public int getLineNumber() {
    return lineNumber;
  }
}
