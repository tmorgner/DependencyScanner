package com.tmorgner.gradle.actions;

import org.gradle.api.provider.Property;
import org.gradle.workers.WorkParameters;

import java.io.File;

public interface DependencyScanWorkParameters extends WorkParameters {
   Property<SourceCollectionType> getClassPathType();
   Property<File> getTarget();
   Property<Integer> getResultTicket();
   Property<Integer> getProjectResolverTicket();
   Property<Boolean> getOnlyScanProjectModules();
}
