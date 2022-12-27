package com.tmorgner.gradle.model.process;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ResultPackage implements Comparable<ResultPackage>{
  private final ResultModule module;
  private final String packageName;
  private final HashMap<String, ResultClass> classes;
  private boolean isProjectPackage;

  public ResultPackage(final ResultModule module,
                       final String packageName) {
    this.packageName = packageName;
    this.module = module;
    this.classes = new HashMap<>();
  }

  public synchronized boolean isProjectPackage() {
    return isProjectPackage;
  }

  public ResultModule getParent() {
    return module;
  }

  public ResultModule getModule() {
    return module;
  }

  public String getPackageName() {
    return packageName;
  }

  public List<ResultClass> getClasses() {
    return new ArrayList<>(classes.values());
  }

  public synchronized ResultClass getOrCreateClass(final String className, boolean projectPackage) {
    if (classes.isEmpty()) {
      isProjectPackage = projectPackage;
      final ResultClass result = new ResultClass(this, className, projectPackage);
      classes.put(className, result);
      return result;
    }
    else {
      ResultClass maybeResult = classes.get(className);
      if (maybeResult != null) {
        return maybeResult;
      }

      ResultClass cls = new ResultClass(this, className, projectPackage);
      classes.put(className, cls);
      return  cls;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final ResultPackage that = (ResultPackage) o;
    return module.equals(that.module) && packageName.equals(that.packageName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(module, packageName);
  }

  @Override
  public int compareTo(@NotNull final ResultPackage o) {
    int cmp = module.compareTo(o.module);
    if (cmp != 0) return cmp;
    return packageName.compareTo(o.packageName);
  }
}

