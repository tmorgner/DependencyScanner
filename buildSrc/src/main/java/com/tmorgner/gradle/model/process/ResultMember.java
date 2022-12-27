package com.tmorgner.gradle.model.process;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ResultMember implements Comparable<ResultMember> {
  private final ResultMemberType type;
  private final ResultClass containedClass;
  private final String methodName;
  private final ArrayList<ResultUsageRecord> usageRecord;

  public ResultMember(final ResultMemberType type,
                      final ResultClass containedClass,
                      final String methodName) {
    this.type = type;
    this.containedClass = containedClass;
    this.methodName = methodName;
    this.usageRecord = new ArrayList<>();
  }

  public ResultMemberType getType() {
    return type;
  }

  public ResultClass getParent() {
    return containedClass;
  }

  public String getMethodName() {
    return methodName;
  }

  public synchronized void recordUsage(ResultUsageRecord r) {
    this.usageRecord.add(r);
  }

  public List<ResultUsageRecord> records() {
    return usageRecord;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ResultMember that = (ResultMember) o;
    return type == that.type && containedClass.equals(that.containedClass) && methodName.equals(that.methodName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, containedClass, methodName);
  }

  @Override
  public int compareTo(@NotNull final ResultMember o) {
    int cmp = containedClass.compareTo(o.containedClass);
    if (cmp != 0) {
      return cmp;
    }

    int cmp2 = getType().compareTo(o.getType());
    if (cmp2 != 0) {
      return cmp2;
    }

    return methodName.compareTo(o.methodName);
  }
}
