package com.tmorgner.gradle;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;

import java.io.File;
import java.util.HashMap;

public class ProjectPathResolver {
  public ProjectPathResolver() {
    projectData = new HashMap<>();
  }

  private final HashMap<String, String> projectData;
  private String projectRoot;

  public void init(Project project) {
    System.out.println("Init " + project.getAllprojects());
    for (Project p : project.getAllprojects()) {
      JavaPluginExtension je = p.getExtensions().getByType(JavaPluginExtension.class);
      System.out.println("  Sources: " + je.getSourceSets().size());

      for (SourceSet mainSource : je.getSourceSets()){
        System.out.println("  Sources: " + mainSource.getName() + " " + mainSource.getOutput());
        for (File f : mainSource.getOutput().getFiles()) {
          projectData.put(f.getAbsolutePath(), p.getPath() + "[" + mainSource.getName() + "]");
          System.out.println("Found source set " + f.getAbsolutePath() + " -> " + p.getPath() + "[" + mainSource.getName() + "]");
        }
      }
    }

    projectRoot = project.getRootDir().getAbsoluteFile().getParentFile().getAbsolutePath();
  }

  public String getProjectForModule(String moduleId) {
    final String projectName = projectData.get(moduleId);
    if (projectName != null) {
      System.out.println("Mapped " + moduleId + " -> " + projectName);
      return projectName;
    }
    return null;
  }

  public String normalize(String moduleId) {

    System.out.println("Normalize " + moduleId);
    if (moduleId.startsWith(projectRoot)) {
      moduleId = moduleId.substring(projectRoot.length());
    }

    moduleId = moduleId.replace('/', '_').replace('\\', '_');
    if (moduleId.startsWith("_")) {
      moduleId = moduleId.substring(1);
    }
    return moduleId;
  }

}
