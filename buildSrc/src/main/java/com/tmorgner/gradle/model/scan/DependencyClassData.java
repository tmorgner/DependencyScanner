package com.tmorgner.gradle.model.scan;

import java.util.*;
import java.util.function.Predicate;

public class DependencyClassData {
  private final String packageName;
  private final String className;
  private final String archive;
  private final String resourcePath;
  private final boolean projectClass;
  private final HashMap<DependencyUsageLocationRecord, HashSet<DependencyTargetRecord>> references;
  private final HashMap<String, DependencyClassMemberRecord> methods;
  private final HashMap<String, DependencyClassMemberRecord> fields;
  private final Predicate<DependencyTargetRecord> targetRecordFilter;
  private String sourceFile;

  public DependencyClassData(final String className,
                             final String archive,
                             final String resourcePath,
                             final boolean isProjectClass,
                             final Predicate<DependencyTargetRecord> filter) {
    this.className = className;
    this.archive = archive;
    this.resourcePath = resourcePath;
    this.projectClass = isProjectClass;
    this.references = new HashMap<>();
    this.methods = new HashMap<>();
    this.fields = new HashMap<>();
    this.targetRecordFilter = filter;
    int packageIndex = this.className.lastIndexOf('.');
    if (packageIndex == -1) {
      packageName = "";
    }
    else {
      packageName = this.className.substring(0, packageIndex);
    }
  }

  public boolean isProjectClass() {
    return projectClass;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getArchive() {
    return archive;
  }

  public String getResourcePath() {
    return resourcePath;
  }

  public String getClassName() {
    return className;
  }

  public String getSourceFile() {
    return sourceFile;
  }

  public void setSourceFile(final String sourceFile) {
    this.sourceFile = sourceFile;
  }

  public Collection<DependencyClassMemberRecord> getFields() {
    return Collections.unmodifiableCollection(fields.values());
  }

  public Collection<DependencyClassMemberRecord> getMethods() {
    return Collections.unmodifiableCollection(methods.values());
  }

  public Set<DependencyUsageLocationRecord> keys() {
    return Collections.unmodifiableSet(references.keySet());
  }

  public Set<DependencyTargetRecord> get(DependencyUsageLocationRecord r) {
    return Collections.unmodifiableSet(references.get(r));
  }

  public void forEach(DependencyClassAction action) {
    for (final Map.Entry<DependencyUsageLocationRecord, HashSet<DependencyTargetRecord>> value : references.entrySet()) {
      for (final DependencyTargetRecord dependencyTargetRecord : value.getValue()) {
        action.apply(value.getKey(), dependencyTargetRecord);
      }
    }
  }

  public void record(final DependencyUsageLocationRecord where, final DependencyTargetRecord what) {
    if (targetRecordFilter != null && !targetRecordFilter.test(what)) {
      return;
    }

    HashSet<DependencyTargetRecord> records = references.computeIfAbsent(where, k -> new HashSet<>());
    records.add(what);
  }

  public void recordMethod(final String memberName) {
    methods.put(memberName, new DependencyClassMemberRecord(-1, memberName, DependencyUsageType.Method));
  }

  public void recordField(final String fieldName) {
    fields.put(fieldName, new DependencyClassMemberRecord(-1, fieldName, DependencyUsageType.Field));
  }
}
