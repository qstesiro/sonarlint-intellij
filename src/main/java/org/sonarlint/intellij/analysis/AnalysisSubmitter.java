/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015-2023 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij.analysis;

import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.changes.ChangeListManager;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.CheckForNull;
import org.sonarlint.intellij.actions.ShowReportCallable;
import org.sonarlint.intellij.actions.ShowSecurityHotspotCallable;
import org.sonarlint.intellij.actions.ShowUpdatedCurrentFileCallable;
import org.sonarlint.intellij.actions.UpdateSinceLastCommitPanelCallable;
import org.sonarlint.intellij.analysis.cayc.CleanAsYouCodeFindingsHolder;
import org.sonarlint.intellij.common.ui.SonarLintConsole;
import org.sonarlint.intellij.tasks.TaskRunnerKt;
import org.sonarlint.intellij.trigger.TriggerType;
import org.sonarlint.intellij.ui.SonarLintToolWindowFactory;

import static org.sonarlint.intellij.common.util.SonarLintUtils.getService;
import static org.sonarlint.intellij.config.Settings.getGlobalSettings;
import static org.sonarlint.intellij.util.ProjectUtils.getAllFiles;

public class AnalysisSubmitter {

  public static final String ANALYSIS_TASK_TITLE = "SonarLint Analysis";
  private final Project project;
  private final OnTheFlyFindingsHolder onTheFlyFindingsHolder;
  private final CleanAsYouCodeFindingsHolder cleanAsYouCodeFindingsHolder;

  public AnalysisSubmitter(Project project) {
    this.project = project;
    this.onTheFlyFindingsHolder = new OnTheFlyFindingsHolder(project);
    this.cleanAsYouCodeFindingsHolder = new CleanAsYouCodeFindingsHolder(project);
  }

  public void analyzeAllFiles() {
    var allFiles = getAllFiles(project);
    var callback = new ShowReportCallable(project);
    var analysis = new Analysis(project, allFiles, TriggerType.ALL, true, callback);
    TaskRunnerKt.startBackgroundableModalTask(project, ANALYSIS_TASK_TITLE, analysis::run);
  }

  public void analyzeVcsChangedFiles() {
    var changedFiles = ChangeListManager.getInstance(project).getAffectedFiles();
    var callback = new ShowReportCallable(project);
    var analysis = new Analysis(project, changedFiles, TriggerType.CHANGED_FILES, true, callback);
    TaskRunnerKt.startBackgroundableModalTask(project, ANALYSIS_TASK_TITLE, analysis::run);
  }

  public void autoAnalyzeOpenFiles(TriggerType triggerType) {
    var openFiles = FileEditorManager.getInstance(project).getOpenFiles();
    // temporarily comment on-the-fly-behavior
    //autoAnalyzeFiles(List.of(openFiles), triggerType);
  }

  public Cancelable autoAnalyzeChangedFiles(Set<VirtualFile> files, TriggerType triggerType) {
    if (!getGlobalSettings().isAutoTrigger()) {
      return null;
    }
    var callback = new UpdateSinceLastCommitPanelCallable(cleanAsYouCodeFindingsHolder);
    return analyzeInBackground(files, triggerType, callback);
  }

  public void autoAnalyzeFile(VirtualFile file, TriggerType triggerType) {
    autoAnalyzeFiles(Collections.singleton(file), triggerType);
  }

  @CheckForNull
  public Cancelable autoAnalyzeFiles(Collection<VirtualFile> files, TriggerType triggerType) {
    if (!getGlobalSettings().isAutoTrigger()) {
      return null;
    }
    //var callback = new UpdateOnTheFlyFindingsCallable(onTheFlyFindingsHolder);
    //return analyzeInBackground(files, triggerType, callback);
    return autoAnalyzeChangedFiles(new HashSet<>(files), triggerType);
  }

  @CheckForNull
  public AnalysisResult analyzeFilesPreCommit(Collection<VirtualFile> files) {
    var console = getService(project, SonarLintConsole.class);
    var trigger = TriggerType.CHECK_IN;
    console.debug("Trigger: " + trigger);
    if (shouldSkipAnalysis()) {
      return null;
    }

    var callback = new ErrorAwareAnalysisCallback();
    var analysis = new Analysis(project, files, trigger, true, callback);
    var result = TaskRunnerKt.runModalTaskWithResult(project, ANALYSIS_TASK_TITLE, analysis::run);
    return callback.analysisSucceeded() ? result : null;
  }

  public void analyzeFilesOnUserAction(Collection<VirtualFile> files, AnActionEvent actionEvent) {
    AnalysisCallback callback;

    if (SonarLintToolWindowFactory.TOOL_WINDOW_ID.equals(actionEvent.getPlace())) {
      callback = new ShowUpdatedCurrentFileCallable(project, onTheFlyFindingsHolder);
    } else {
      callback = new ShowReportCallable(project);
    }

    // do we really need to distinguish both cases ? Couldn't we always run in background ?
    if (shouldExecuteInBackground(actionEvent)) {
      analyzeInBackground(files, TriggerType.ACTION, callback);
    } else {
      analyzeInBackgroundableModal(files, TriggerType.ACTION, callback);
    }
  }

  public void analyzeFileAndTrySelectHotspot(VirtualFile file, String securityHotspotKey) {
    AnalysisCallback callback = new ShowSecurityHotspotCallable(project, onTheFlyFindingsHolder, securityHotspotKey);
    var task = new Analysis(project, List.of(file), TriggerType.OPEN_SECURITY_HOTSPOT, true, callback);
    TaskRunnerKt.startBackgroundableModalTask(project, ANALYSIS_TASK_TITLE, task::run);
  }

  /**
   * Whether the analysis should be launched in the background.
   * Analysis should be run in background in the following cases:
   * - Keybinding used (place = MainMenu)
   * - Macro used (place = unknown)
   * - Action used, ctrl+shift+A (place = GoToAction)
   */
  private static boolean shouldExecuteInBackground(AnActionEvent e) {
    return ActionPlaces.isMainMenuOrActionSearch(e.getPlace())
      || ActionPlaces.UNKNOWN.equals(e.getPlace());
  }

  private Cancelable analyzeInBackground(Collection<VirtualFile> files, TriggerType trigger, AnalysisCallback callback) {
    var task = new Analysis(project, files, trigger, false, callback);
    TaskRunnerKt.startBackgroundTask(project, ANALYSIS_TASK_TITLE, task::run);
    return task;
  }

  private void analyzeInBackgroundableModal(Collection<VirtualFile> files, TriggerType action, AnalysisCallback callback) {
    if (shouldSkipAnalysis()) {
      return;
    }
    var analysis = new Analysis(project, files, action, true, callback);
    TaskRunnerKt.startBackgroundableModalTask(project, ANALYSIS_TASK_TITLE, analysis::run);
  }

  private boolean shouldSkipAnalysis() {
    var status = getService(project, AnalysisStatus.class);
    var console = getService(project, SonarLintConsole.class);
    if (project.isDisposed() || !status.tryRun()) {
      console.info("Canceling analysis triggered by the user because another one is already running or because the project is disposed");
      return true;
    }
    return false;
  }

  public void clearCurrentFileIssues() {
    onTheFlyFindingsHolder.clearCurrentFile();
  }

  private static class ErrorAwareAnalysisCallback implements AnalysisCallback {
    private final AtomicBoolean errored = new AtomicBoolean(false);

    @Override
    public void onSuccess(AnalysisResult analysisResult) {
      // do nothing
    }

    @Override
    public void onError(Throwable e) {
      errored.set(true);
    }

    public boolean analysisSucceeded() {
      return !errored.get();
    }

  }
}
