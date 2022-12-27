package com.tmorgner.gradle.actions;

import com.tmorgner.gradle.model.process.*;
import com.tmorgner.gradle.model.scan.DependencyClassData;
import com.tmorgner.gradle.model.scan.DependencyTargetRecord;
import com.tmorgner.gradle.model.scan.DependencyUsageLocationRecord;
import com.tmorgner.gradle.model.scan.DependencyUsageType;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.workers.WorkAction;

import java.util.HashSet;

public abstract class DependencyProcessWorkAction implements WorkAction<DependencyProcessWorkParameters> {

  private final Logger logger = Logging.getLogger(DependencyProcessWorkAction.class);

  @Override
  public void execute() {
    try {
      final DependencyProcessWorkParameters parameters = getParameters();
      final HashSet<String> packages = new HashSet<>(parameters.getPackages().get());
      final DependencyScanResult scanResult =
          ResultReceiver.getInstance(DependencyScanResult.class).getResult(parameters.getDataTicket().get());
      final ResultData resultData =
          ResultReceiver.getInstance(ResultData.class).getResult(parameters.getResultTicket().get());

      for (final DependencyClassData dependencyClassData : scanResult.getClasses()) {
        for (final DependencyUsageLocationRecord usageSource : dependencyClassData.keys()) {
          for (final DependencyTargetRecord target : dependencyClassData.get(usageSource)) {
            if (!packages.contains(target.packageName)) {
              continue;
            }

            processEntry(scanResult, resultData, target, dependencyClassData, usageSource);
          }
        }
      }
    } catch (Exception e) {
      logger.error("Unexpected error while executing DependencyScanWorkAction", e);
      throw e;
    }
  }


  void processEntry(final DependencyScanResult scanResult,
                    final ResultData resultData,
                    final DependencyTargetRecord usageTarget,
                    final DependencyClassData usageClass,
                    final DependencyUsageLocationRecord usageLocation) {
    DependencyClassData c = scanResult.get(usageTarget.className);
    boolean referencesProjectClass;
    if (c == null) {
      referencesProjectClass = false;
    }
    else {
      referencesProjectClass = c.isProjectClass();
    }

    final ResultModule module = resultData.getModuleForClass(usageTarget.className);
    final ResultPackage pkg = module.getOrCreatePackage(usageTarget.packageName);
    final ResultClass cls = pkg.getOrCreateClass(usageTarget.className, referencesProjectClass);
    final ResultMember member =
        (usageTarget.memberType == DependencyUsageType.Method) ?
            cls.getOrCreateMethod(usageTarget.member) :
            cls.getOrCreateField(usageTarget.member);

    member.recordUsage(new ResultUsageRecord(member,
                                             usageLocation.usageType,
                                             usageClass.getArchive(),
                                             usageClass.getPackageName(),
                                             usageClass.getClassName(),
                                             usageLocation.memberName,
                                             usageLocation.lineNumber));
  }
}

