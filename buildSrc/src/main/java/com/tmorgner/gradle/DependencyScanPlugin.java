package com.tmorgner.gradle;

import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.jvm.JvmTestSuite;
import org.gradle.api.tasks.SourceSet;
import org.gradle.testing.base.TestingExtension;
import org.jetbrains.annotations.NotNull;

public class DependencyScanPlugin implements Plugin<Project> {

  @Override
  public void apply(@NotNull final Project target) {
  }

  @SuppressWarnings("UnstableApiUsage")
  public static void configureTask(Project project, final DependencyTask t) {
    JavaPluginExtension javaExtension = project.getExtensions().getByType(JavaPluginExtension.class);
    SourceSet mainSourceSet = javaExtension.getSourceSets().maybeCreate(SourceSet.MAIN_SOURCE_SET_NAME);
    t.classpath(mainSourceSet.getOutput());

    TestingExtension testing = project.getExtensions().getByType(TestingExtension.class);
    final NamedDomainObjectSet<JvmTestSuite> testSuite = testing.getSuites().withType(JvmTestSuite.class);
    testSuite.forEach(suite -> {
      final SourceSet testSourceSet = suite.getSources();
      final FileCollection testSourceSetOutput = testSourceSet.getOutput();
      t.testClasspath(testSourceSetOutput);
    });
  }


}
