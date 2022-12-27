package com.tmorgner.gradle.model.scan;

import java.util.Objects;

/**
 * Records the target of a member call or field access.
 */
public class DependencyTargetRecord {
  public final String className;
  public final String member;
  public final DependencyUsageType memberType;
  public final String packageName;

  public DependencyTargetRecord(final String className,
                                final String member,
                                final DependencyUsageType memberType) {
    if (className.contains("/")) {
      this.className = className.replace('/', '.');
    }
    else {
      this.className = className;
    }

    this.member = member;
    this.memberType = memberType;

    int packageIndex = this.className.lastIndexOf('.');
    if (packageIndex == -1) {
      packageName = "";
    }
    else {
      packageName = this.className.substring(0, packageIndex);
    }
  }

  @Override
  public String toString() {
    if (memberType == DependencyUsageType.TypeReference) {
      return className + " [" + memberType + ']';
    }

    return className + " # " + member + " [" + memberType + ']';
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final DependencyTargetRecord that = (DependencyTargetRecord) o;
    return Objects.equals(className, that.className) && Objects.equals(member, that.member) && memberType == that.memberType;
  }

  @Override
  public int hashCode() {
    return Objects.hash(className, member, memberType);
  }
}
