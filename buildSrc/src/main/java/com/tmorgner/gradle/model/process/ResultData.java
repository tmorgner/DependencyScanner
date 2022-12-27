package com.tmorgner.gradle.model.process;

import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.util.*;
import java.util.stream.Stream;

public class ResultData {

  private final Logger logger = Logging.getLogger(ResultData.class);

  final HashMap<String, ResultModule> classToModuleMapping;
  final HashMap<String, ResultClass> resultClassByName;
  final HashMap<String, ResultModule> resultModuleByName;
  final HashMap<String, Set<ResultPackage>> packagesByName;
  final ResultModule fallbackModule;

  public ResultData() {
    fallbackModule = new ResultModule("");
    classToModuleMapping = new HashMap<>();
    resultClassByName = new HashMap<>();
    resultModuleByName = new HashMap<>();
    packagesByName = new HashMap<>();
  }

  public void registerClassToModuleMapping(String module, String className) {
    logger.debug("Mapping class " + className + " to module " + module);
    ResultModule mod = resultModuleByName.computeIfAbsent(module, ResultModule::new);
    classToModuleMapping.put(className, mod);
  }

  public void buildIndex() {
    for (final ResultModule module : classToModuleMapping.values()) {
      for (final ResultPackage pkg : module.getPackages()) {
        for (final ResultClass cls : pkg.getClasses()) {
          resultClassByName.put(cls.getClassName(), cls);
        }

        Set<ResultPackage> p = packagesByName.computeIfAbsent(pkg.getPackageName(), n -> new HashSet<>());
        p.add(pkg);
      }
    }
  }

  public Set<ResultPackage> getAllPackageLocations(String pkg) {
    return packagesByName.getOrDefault(pkg, Collections.emptySet());
  }

  public ResultClass getClassForName(String name) {
    return resultClassByName.get(name);
  }

  public Stream<ResultModule> getModules() {
    return Stream.concat(Stream.of(fallbackModule), classToModuleMapping.values().stream());
  }

  public ResultModule getModuleForClass(String className) {
    ResultModule m = classToModuleMapping.get(className);
    if (m == null) {
      logger.debug("Unable to map class " + className + " to any module");
      return fallbackModule;
    }

    return m;
  }

  public ResultModule getModule(final String moduleId) {
    if ("".equals(moduleId)) {
      return fallbackModule;
    }

    for (final ResultModule value : classToModuleMapping.values()) {
      if (value.getModuleName().equals(moduleId)) {
        return value;
      }
    }
    throw new GradleException("Unable to locate module with id " + moduleId);
  }
}
