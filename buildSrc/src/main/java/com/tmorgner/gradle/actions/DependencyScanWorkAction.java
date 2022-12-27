package com.tmorgner.gradle.actions;

import com.tmorgner.gradle.ProjectPathResolver;
import com.tmorgner.gradle.model.scan.DependencyClassData;
import com.tmorgner.gradle.model.scan.DependencyTargetRecord;
import com.tmorgner.gradle.processors.ClassProcessor;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.workers.WorkAction;
import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public abstract class DependencyScanWorkAction implements WorkAction<DependencyScanWorkParameters> {

  private final Logger logger = Logging.getLogger(DependencyScanWorkAction.class);

  @Override
  public void execute() {
    try {
      SourceCollectionType type = getParameters().getClassPathType().get();
      File target = getParameters().getTarget().get();
      if (target.isDirectory()) {
        // this is a uncompressed classpath element; probably a set of compiled classes.
        logger.debug(type + " -> [dir] " + target);
        processDirectory(target.toPath());
      }
      else if (target.isFile()) {
        String ext = FilenameUtils.getExtension(target.getName());
        logger.debug(type + " -> [" + ext + "] " + target);
        if (ext.equals("zip") || ext.equals("jar")) {
          // jars are zip files, and zip files are valid classpath elements
          try {
            processZip(target);
          } catch (IOException e) {
            throw new GradleException("Failed to process Zip", e);
          }
        }
      }
    } catch (Throwable e) {
      logger.error("Unexpected error while executing DependencyScanWorkAction", e);
      throw e;
    }
  }

  public void processZip(File zip) throws IOException {

    logger.debug("Processing jar file " + zip);
    boolean onlyScanModules = getParameters().getOnlyScanProjectModules().get();

    try (FileSystem fs = FileSystems.newFileSystem(zip.toPath(), null)) {
      fs.getRootDirectories().forEach(root -> {
        // in a full implementation, you'd have to
        // handle directories
        try (final Stream<Path> pathStream = Files.walk(root)) {
          pathStream.filter(Files::isRegularFile)
                    .filter(f -> "class".equals(FilenameUtils.getExtension(f.toString())))
                    .forEach(path -> {

                      try {
                        String name = root.relativize(path).toString();
                        byte[] content = Files.readAllBytes(path);
                        processClass(zip.getName(), name, !onlyScanModules, content);
                      } catch (IOException e) {
                        throw new GradleException("1", e);
                      }
                    });
        } catch (IOException e) {
          throw new GradleException("2", e);
        }
      });

    }
  }

  public void processDirectory(Path directory) {
    int ticket = getParameters().getProjectResolverTicket().get();
    boolean onlyScanModules = getParameters().getOnlyScanProjectModules().get();

    ProjectPathResolver p = ResultReceiver.getInstance(ProjectPathResolver.class).getResult(ticket);

    String moduleId = p.getProjectForModule(directory.toString());
    boolean isProject;
    if (moduleId == null) {
      moduleId = p.normalize(directory.toString());
      isProject = !onlyScanModules;
    }
    else {
      isProject = true;
    }

    final String moduleIdLast = moduleId;
    try (final Stream<Path> pathStream = Files.walk(directory)) {
      pathStream.filter(f -> "class".equals(FilenameUtils.getExtension(f.toString())))
                .forEach(f -> processFile(moduleIdLast, directory, isProject, f));

    } catch (IOException ioe) {
      throw new GradleException("Unable to traverse directory " + directory, ioe);
    }
  }

  private void processFile(String moduleId, Path directory, boolean isProject, Path target) {
    try {
      logger.debug("Processing file " + target);
      String path = directory.relativize(target).toString();
      byte[] bytes = Files.readAllBytes(target);
      processClass(moduleId, path, isProject, bytes);
    } catch (IOException ioe) {
      throw new GradleException("Unable to read file " + target, ioe);
    }
  }

  private void processClass(String archive, String path, boolean isProject, byte[] byteCode) {

    ClassReader r = new ClassReader(byteCode);
    final String className = r.getClassName().replace("\\", ".").replace("/", ".");
    logger.debug(" * " + className);

    DependencyClassData cd = new DependencyClassData(className, archive, path, isProject, DependencyScanWorkAction::acceptReference);
    r.accept(new ClassProcessor(cd, isProject), 0);
    int ticket = getParameters().getResultTicket().get();
    ResultReceiver.getInstance(DependencyScanResult.class).getResult(ticket).add(cd);
  }

  private static boolean acceptReference(DependencyTargetRecord r) {
    if (r.className.startsWith("java.")) {
      return false;
    }
    if (r.className.startsWith("javax.")) {
      return false;
    }
    if (r.className.startsWith("sun.")) {
      return false;
    }
    if (r.className.startsWith("com.sun.")) {
      return false;
    }
    if (r.className.startsWith("jdk.")) {
      return false;
    }
    return true;
  }

}

