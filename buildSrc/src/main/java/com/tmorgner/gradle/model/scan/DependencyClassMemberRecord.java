package com.tmorgner.gradle.model.scan;

/**
 * Records the existence of a class member.
 */
public class DependencyClassMemberRecord {
  public int lineNumber;
  public String memberName;
  public DependencyUsageType usageType;

  public DependencyClassMemberRecord(final int lineNumber,
                                     final String memberName,
                                     final DependencyUsageType usageType) {
    this.lineNumber = lineNumber;
    this.memberName = memberName;
    this.usageType = usageType;
  }
}
