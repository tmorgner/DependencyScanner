package com.tmorgner.gradle.model.scan;

@FunctionalInterface
public interface DependencyClassAction {
  void apply(DependencyUsageLocationRecord source, DependencyTargetRecord target);
}
