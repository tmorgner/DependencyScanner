package com.tmorgner.gradle.actions;

import com.tmorgner.gradle.model.process.*;
import org.gradle.api.GradleException;
import org.gradle.workers.WorkAction;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PrintPlainTextWorkAction implements WorkAction<PrintResultWorkActionParameters> {
  @Override
  public void execute() {
    final String moduleId = getParameters().getModule().get();
    final Integer ticket = getParameters().getResultTicket().get();
    final ResultData resultData = ResultReceiver.getInstance(ResultData.class).getResult(ticket);

    final ResultModule module = resultData.getModule(moduleId);

    generatePackageReport(moduleId, resultData, module);
    generateClassReport(moduleId, resultData, module);
  }

  private void generateClassReport(final String moduleId, final ResultData resultData, final ResultModule module) {
    if (!module.isProjectModule()) {
      return;
    }

    final File outputDir = getParameters().getOutputDirectory().get();
    final File file = new File(outputDir, "classes_" + moduleIdToPath(moduleId) + ".md");
    try {
      try (PrintWriter writer = new PrintWriter(file, StandardCharsets.ISO_8859_1.name())) {
        writer.println("# Module " + moduleId);
        writer.println();
        for (final ResultPackage p : module.getPackages()
                                           .stream()
                                           .sorted()
                                           .collect(Collectors.toList())) {
          writer.println("## Package " + p.getPackageName());
          writer.println();
          printPackageLevelResults(writer, resultData, p);

          for (final ResultClass c : p.getClasses()
                                      .stream()
                                      .sorted()
                                      .collect(Collectors.toList())) {

            writer.println("### Class `" + c.getClassName() + "`");
            writer.println();
            printClassLevelResults(writer, resultData, c);
          }
        }
      } catch (IOException e) {
        e.printStackTrace();
        throw new GradleException("Unable to write file", e);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new GradleException(e.getMessage(), e);
    }
  }

  private void generatePackageReport(final String moduleId, final ResultData resultData, final ResultModule module) {
    if (!module.isProjectModule()) {
      return;
    }

    final File outputDir = getParameters().getOutputDirectory().get();
    final File file = new File(outputDir, "package_" + moduleIdToPath(moduleId) + ".md");
    try {
      try (PrintWriter writer = new PrintWriter(file, StandardCharsets.ISO_8859_1.name())) {
        writer.println("# " + moduleId);
        writer.println();
        for (final ResultPackage p : module.getPackages()
                                           .stream()
                                           .sorted()
                                           .collect(Collectors.toList())) {
          writer.println("## Package " + p.getPackageName());
          writer.println();
          printPackageLevelResults(writer, resultData, p);
        }
      } catch (IOException e) {
        e.printStackTrace();
        throw new GradleException("Unable to write file", e);
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new GradleException(e.getMessage(), e);
    }
  }

  private void printPackageLevelResults(PrintWriter writer,
                                        ResultData data,
                                        ResultPackage p) {

    final HashSet<String> packagesUsed = new HashSet<>();
    for (final ResultClass c : p.getClasses()) {
      Stream.concat(c.fields(), c.methods()).forEach(m -> {
        for (final ResultUsageRecord record : m.records()) {
          packagesUsed.add(record.getPackageName());
        }
      });
    }

    Set<ResultPackage> packageSet = data.getAllPackageLocations(p.getPackageName());
    if (packageSet != null && packageSet.size() > 1) {
      writer.println("Classes from this package are contained in multiple modules:");
      writer.println();
      for (final ResultPackage resultPackage : packageSet) {
        writer.print("* ");
        printModuleLink(writer, resultPackage.getModule().getModuleName());
        writer.println();
      }
    }

    writer.println("Packages that directly reference this package:");
    writer.println();

    for (String pkg : packagesUsed.stream().sorted(String::compareTo).collect(Collectors.toList())) {
      writer.print("* ");
      printPackageLink(writer, pkg);
      writer.println();
    }
    writer.println();
  }

  private static void printPackageLink(final PrintWriter writer,
                                       final String pkg) {
    writer.print("[");
    writer.print(pkg);
    writer.print("](#");
    writer.print("Package-");
    writer.print(pkg);
    writer.print(")");
  }

  private static void printModuleLink(final PrintWriter writer,
                                      final String module) {
    writer.print("[");
    writer.print(module);
    writer.print("](#");
    writer.print("Module-");
    writer.print(module);
    writer.print(")");
  }

  private static void printClassLink(final PrintWriter writer,
                                     final String cls) {
    writer.print("[");
    writer.print(cls);
    writer.print("](#");
    writer.print("Class-");
    writer.print(cls);
    writer.print(")");
  }

  private void printClassLevelResults(PrintWriter writer,
                                      ResultData data,
                                      ResultClass p) {

    final HashSet<ResultClass> classesUsed = new HashSet<>();
    for (final ResultMember c : Stream.concat(p.fields(), p.methods())
                                      .sorted()
                                      .collect(Collectors.toList())) {
      c.records().forEach(r -> {
        ResultClass cls = data.getClassForName(r.getClassName());
        if (cls != null) {
          classesUsed.add(cls);
        }
      });
    }

    writer.println("Classes that directly reference members of this class:");
    writer.println();

    Map<ResultPackage, List<ResultClass>> pnc = classesUsed.stream()
                                                           .sorted()
                                                           .collect(Collectors.groupingBy(ResultClass::getParent));
    for (final Map.Entry<ResultPackage, List<ResultClass>> entry : pnc.entrySet()) {
      writer.print("* ");
      printPackageLink(writer, entry.getKey().getPackageName());
      writer.println();

      for (final ResultClass c : entry.getValue()) {
        writer.print("  * ");
        printClassLink(writer, c.getClassName());
        writer.println();
      }
    }
    writer.println();
  }

  String moduleIdToPath(String moduleId) {

    if ("".equals(moduleId)) {
      return "jdk_";
    }

    String normalizedPath = moduleId.replace("\\", "/");
    int lastPathSeparator = normalizedPath.lastIndexOf("/");
    if (lastPathSeparator != -1) {
      normalizedPath = normalizedPath.substring(lastPathSeparator + 1);
    }

    return "module_" + normalizedPath.replace(':', '_');
  }

}
