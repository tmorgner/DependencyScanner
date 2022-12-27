package com.tmorgner.gradle.model.process;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class ResultModule implements Comparable<ResultModule> {
  private final String moduleName;
  private final HashMap<String, ResultPackage> packages;
  private final Function<String, ResultPackage> supplier;

  public ResultModule(final String moduleName) {
    this.moduleName = moduleName;
    this.packages = new HashMap<>();
    this.supplier = p -> new ResultPackage(this, p);
  }

  public String getModuleName() {
    return moduleName;
  }

  public synchronized ResultPackage getOrCreatePackage(final String packageName) {
    return packages.computeIfAbsent(packageName, supplier);
  }

  public List<ResultPackage> getPackages() {
    return new ArrayList<>(packages.values());
  }

  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isProjectModule () {
    for (final ResultPackage value : packages.values()) {
      if (value.isProjectPackage()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ResultModule that = (ResultModule) o;
    return Objects.equals(moduleName, that.moduleName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(moduleName);
  }

  @Override
  public int compareTo(@NotNull final ResultModule o) {
    return moduleName.compareTo(o.moduleName);
  }
}
