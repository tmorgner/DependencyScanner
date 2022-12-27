package com.tmorgner.gradle.actions;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

import java.io.File;

public interface PrintResultWorkActionParameters extends WorkParameters {
  Property<Integer> getResultTicket();
  Property<String> getModule();
  Property<File> getOutputDirectory();
}
