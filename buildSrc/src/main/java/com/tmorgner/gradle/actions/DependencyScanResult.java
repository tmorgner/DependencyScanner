package com.tmorgner.gradle.actions;

import com.tmorgner.gradle.model.scan.DependencyClassData;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

public class DependencyScanResult  {
  private final HashMap<String, DependencyClassData> classData;

  public DependencyScanResult() {
    this.classData = new HashMap<>();
  }

  public void add(DependencyClassData c) {
    classData.put(c.getClassName(), c);
  }

  public Collection<DependencyClassData> getClasses() {
    return Collections.unmodifiableCollection(classData.values());
  }

  public DependencyClassData get(String className) {
    return classData.get(className);
  }
}
