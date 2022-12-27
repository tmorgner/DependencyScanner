package com.tmorgner.gradle.model.process;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public class ResultClass implements Comparable<ResultClass>{
  private final ResultPackage containedPackage;
  private final String className;
  private final boolean projectClass;
  private final HashMap<String, ResultMember> methods;
  private final HashMap<String, ResultMember> fields;
  private final Function<String, ResultMember> methodSupplier;
  private final Function<String, ResultMember> fieldSupplier;

  public ResultClass(final ResultPackage containedPackage,
                     final String className,
                     final boolean projectClass) {
    this.containedPackage = containedPackage;
    this.className = className;
    this.projectClass = projectClass;
    this.methods = new HashMap<>();
    this.fields = new HashMap<>();
    this.methodSupplier = m -> new ResultMember(ResultMemberType.Method, this, m);
    this.fieldSupplier = m -> new ResultMember(ResultMemberType.Field, this, m);
  }

  public boolean isProjectClass() {
    return projectClass;
  }

  public Stream<ResultMember> fields() {
    return fields.values().stream();
  }

  public Stream<ResultMember> methods() {
    return methods.values().stream();
  }

  public  ResultPackage getParent(){
    return containedPackage;
  }

  public String getClassName() {
    return className;
  }

  public synchronized ResultMember getOrCreateMethod(final String methodName) {
    return methods.computeIfAbsent(methodName, methodSupplier);
  }

  public synchronized ResultMember getOrCreateField(final String methodName) {
    return fields.computeIfAbsent(methodName, fieldSupplier);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ResultClass that = (ResultClass) o;
    return containedPackage.equals(that.containedPackage) && className.equals(that.className);
  }

  @Override
  public int hashCode() {
    return Objects.hash(containedPackage, className);
  }

  @Override
  public int compareTo(@NotNull final ResultClass o) {
    int cmp = containedPackage.compareTo(o.containedPackage);
    if (cmp != 0) return cmp;

    return className.compareTo(o.className);
  }
}

