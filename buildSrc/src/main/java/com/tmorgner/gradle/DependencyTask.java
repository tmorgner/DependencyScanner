package com.tmorgner.gradle;

import com.tmorgner.gradle.actions.*;
import com.tmorgner.gradle.model.process.ResultData;
import com.tmorgner.gradle.model.scan.DependencyClassData;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileCollection;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.workers.WorkQueue;
import org.gradle.workers.WorkerExecutor;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public abstract class DependencyTask extends DefaultTask {

  private ConfigurableFileCollection classpath;
  private ConfigurableFileCollection testClassPath;
  private boolean onlyScanProjectModules;

  public DependencyTask() {
    setDescription("Collects dependency data");
    setGroup("Report");
    onlyScanProjectModules = true;
    DependencyScanPlugin.configureTask(getProject(), this);
  }

  @Inject
  abstract public WorkerExecutor getWorkerExecutor();

  @Input
  @SuppressWarnings("unused")
  public boolean getOnlyScanProjectModules() {
    return onlyScanProjectModules;
  }

  @SuppressWarnings("unused")
  public void setOnlyScanProjectModules(final boolean onlyScanProjectModules) {
    this.onlyScanProjectModules = onlyScanProjectModules;
  }

  /**
   * Returns the classpath to include in the WAR archive. Any JAR or ZIP files in this classpath are included in the
   * {@code WEB-INF/lib} directory. Any directories in this classpath are included in the {@code WEB-INF/classes}
   * directory.
   *
   * @return The classpath. Returns an empty collection when there is no classpath to include in the WAR.
   */
  @Nullable
  @Optional
  @Classpath
  public FileCollection getClasspath() {
    return classpath;
  }

  /**
   * Sets the classpath to include in the WAR archive.
   *
   * @param classpath
   *     The classpath. Must not be null.
   * @since 4.0
   */
  public void setClasspath(FileCollection classpath) {
    setClasspath((Object) classpath);
  }

  /**
   * Sets the classpath to include in the WAR archive.
   *
   * @param classpath
   *     The classpath. Must not be null.
   */
  public void setClasspath(Object classpath) {
    this.classpath = getProject().files(classpath);
  }

  /**
   * Adds files to the classpath to include in the WAR archive.
   *
   * @param classpath
   *     The files to add. These are evaluated as per {@link org.gradle.api.Project#files(Object...)}
   */
  public void classpath(Object... classpath) {
    FileCollection oldClasspath = getClasspath();
    this.classpath = getProject().files(oldClasspath != null ? oldClasspath : new ArrayList<>(), classpath);
  }

  /**
   * Returns the classpath to include in the WAR archive. Any JAR or ZIP files in this classpath are included in the
   * {@code WEB-INF/lib} directory. Any directories in this classpath are included in the {@code WEB-INF/classes}
   * directory.
   *
   * @return The classpath. Returns an empty collection when there is no classpath to include in the WAR.
   */
  @Nullable
  @Optional
  @Classpath
  public FileCollection getTestClasspath() {
    return testClassPath;
  }

  /**
   * Sets the classpath to include in the WAR archive.
   *
   * @param classpath
   *     The classpath. Must not be null.
   * @since 4.0
   */
  public void setTestClasspath(FileCollection classpath) {
    setTestClasspath((Object) classpath);
  }

  /**
   * Sets the classpath to include in the WAR archive.
   *
   * @param classpath
   *     The classpath. Must not be null.
   */
  public void setTestClasspath(Object classpath) {
    this.testClassPath = getProject().files(classpath);
  }

  /**
   * Adds files to the classpath to include in the WAR archive.
   *
   * @param classpath
   *     The files to add. These are evaluated as per {@link org.gradle.api.Project#files(Object...)}
   */
  public void testClasspath(Object... classpath) {
    FileCollection oldClasspath = getTestClasspath();
    this.testClassPath = getProject().files(oldClasspath != null ? oldClasspath : new ArrayList<>(), classpath);
  }

  @TaskAction
  public void run() {
    final int ticket = ResultReceiver.getInstance(DependencyScanResult.class).prepareResult();
    final int resultTicket = ResultReceiver.getInstance(ResultData.class).prepareResult();
    final int projectResolverTicket = ResultReceiver.getInstance(ProjectPathResolver.class).prepareResult();
    final ProjectPathResolver r = ResultReceiver.getInstance(ProjectPathResolver.class).getResult(projectResolverTicket);
    r.init(getProject());

    try {
      collectRawDependencyData(projectResolverTicket, ticket);

      final List<String> packageList = extractPackageSet(ticket);

      final ResultData resultData = ResultReceiver.getInstance(ResultData.class).getResult(resultTicket);
      final DependencyScanResult scanResult = ResultReceiver.getInstance(DependencyScanResult.class).getResult(ticket);
      for (final DependencyClassData classData : scanResult.getClasses()) {
        resultData.registerClassToModuleMapping(classData.getArchive(), classData.getClassName());
      }

      processPackages(packageList, ticket, resultTicket);
      resultData.buildIndex();
      printPlainText(resultData, resultTicket);

      this.getProject();
    } catch (Exception e) {
      e.printStackTrace();
      throw new GradleException("Blah", e);
    } finally {
      ResultReceiver.getInstance(DependencyScanResult.class).freeResult(ticket);
      ResultReceiver.getInstance(ResultData.class).freeResult(resultTicket);
      ResultReceiver.getInstance(ProjectPathResolver.class).freeResult(projectResolverTicket);
    }
  }

  private void printPlainText(final ResultData data,
                              final int resultTicket) {

    Directory d = getProject().getLayout().getBuildDirectory().get();
    final File outputDir = d.dir("reports").dir("dependencies").dir("text").getAsFile();
    if (!outputDir.exists() && !outputDir.mkdirs()) {
      throw new GradleException("Unable to create directory " + outputDir);
    }

    WorkQueue workQueue = getWorkerExecutor().noIsolation();
    data.getModules().forEach(m -> workQueue.submit(PrintPlainTextWorkAction.class, parameters -> {
      parameters.getResultTicket().set(resultTicket);
      parameters.getModule().set(m.getModuleName());
      parameters.getOutputDirectory().set(outputDir);
    }));
    workQueue.await();
  }

  private void processPackages(final List<String> packageList,
                               final int ticket,
                               final int resultTicket) {
    System.out.println("Building process package " + ticket);
    final List<List<String>> partitions = partition(packageList, Runtime.getRuntime().availableProcessors());
    WorkQueue workQueue = getWorkerExecutor().noIsolation();
    for (final List<String> partition : partitions) {
      workQueue.submit(DependencyProcessWorkAction.class, parameters -> {
        parameters.getPackages().set(partition);
        parameters.getDataTicket().set(ticket);
        parameters.getResultTicket().set(resultTicket);
      });
    }

    workQueue.await();
  }

  private static List<String> extractPackageSet(final int ticket) {
    HashSet<String> packages = new HashSet<>();
    final DependencyScanResult result = ResultReceiver.getInstance(DependencyScanResult.class).getResult(ticket);
    for (final DependencyClassData classData : result.getClasses()) {
      classData.forEach((s, t) -> packages.add(t.packageName));
    }

    return Collections.unmodifiableList(new ArrayList<>(packages));
  }

  private void collectRawDependencyData(final int projectResolverTicket, final int ticket) {
    WorkQueue workQueue = getWorkerExecutor().noIsolation();
    HashSet<File> knownFiles = new HashSet<>();
    if (classpath != null) {
      for (File sourceFile : this.classpath.getFiles()) {
        if (knownFiles.add(sourceFile)) {
          workQueue.submit(DependencyScanWorkAction.class, parameters -> {
            parameters.getTarget().set(sourceFile);
            parameters.getResultTicket().set(ticket);
            parameters.getProjectResolverTicket().set(projectResolverTicket);
            parameters.getClassPathType().set(SourceCollectionType.Runtime);
            parameters.getOnlyScanProjectModules().set(onlyScanProjectModules);
          });
        }
      }
    }

    if (testClassPath != null) {
      for (File sourceFile : testClassPath.getFiles()) {
        if (knownFiles.add(sourceFile)) {
          workQueue.submit(DependencyScanWorkAction.class, parameters -> {
            parameters.getTarget().set(sourceFile);
            parameters.getResultTicket().set(ticket);
            parameters.getProjectResolverTicket().set(projectResolverTicket);
            parameters.getClassPathType().set(SourceCollectionType.Test);
            parameters.getOnlyScanProjectModules().set(onlyScanProjectModules);
          });
        }
      }
    }
    workQueue.await();
  }

  public <T> List<List<T>> partition(List<T> list, int partitionCount) {
    int partitionSize = (int) Math.ceil(list.size() / (double) partitionCount);
    int startIndex = 0;
    List<List<T>> result = new ArrayList<>();
    while (startIndex < list.size()) {
      result.add(list.subList(startIndex, Math.min(startIndex + partitionSize, list.size())));
      startIndex += 1;
    }
    return result;
  }
}

