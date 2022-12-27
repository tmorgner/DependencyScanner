package com.tmorgner.gradle.model.scan;

/**
 * Records the use of a dependency. A dependency is a pointer to a member of a class.
 *
 * @see DependencyClassMemberRecord
 */
public class DependencyUsageLocationRecord {
  public final int lineNumber;
  public final String memberName;
  public final DependencyUsageType usageType;

  public DependencyUsageLocationRecord(final int lineNumber, final String memberName, final DependencyUsageType usageType) {
    this.lineNumber = lineNumber;
    this.memberName = memberName;
    this.usageType = usageType;
  }

  public DependencyUsageLocationRecord withType(DependencyUsageType t) {
    return new DependencyUsageLocationRecord(lineNumber, memberName, t);
  }

  public DependencyUsageLocationRecord withLineNumber(int ignoredN) {
    // API traces the line numbers, but right now I am not actually recording it
    return this;
  }

  @Override
  public String toString() {
    if (lineNumber >= 0) {
      return "{" + usageType + "}" + memberName + ":" + lineNumber;
    }
    return "{" + usageType + "}" + memberName;
  }

  public DependencyUsageLocationRecord withMethod(final String name) {
    return new DependencyUsageLocationRecord(lineNumber, name, DependencyUsageType.Method);
  }

  public DependencyUsageLocationRecord withField(final String name) {
    return new DependencyUsageLocationRecord(lineNumber, name, DependencyUsageType.Field);
  }
}
