package com.tmorgner.gradle.actions;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

public interface DependencyProcessWorkParameters extends WorkParameters {
  ListProperty<String> getPackages();

  Property<Integer> getResultTicket();
  Property<Integer> getDataTicket();
}
