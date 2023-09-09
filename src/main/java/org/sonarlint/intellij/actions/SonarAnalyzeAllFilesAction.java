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
package org.sonarlint.intellij.actions;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonarlint.intellij.analysis.AnalysisSubmitter;
import org.sonarlint.intellij.analysis.AnalysisStatus;
import org.sonarlint.intellij.common.util.SonarLintUtils;
import com.intellij.openapi.diagnostic.Logger;

import static org.sonarlint.intellij.util.ProjectUtils.hasFiles;

public class SonarAnalyzeAllFilesAction extends AbstractSonarAction {

    static final Logger log = Logger.getInstance(SonarAnalyzeAllFilesAction.class);

    private static final String HIDE_WARNING_PROPERTY = "SonarLint.analyzeAllFiles.hideWarning";

    public SonarAnalyzeAllFilesAction() {
        super();
        log.info("--- SonarAnalyzeAllFilesAction.SonarAnalyzeAllFilesAction");
    }

    public SonarAnalyzeAllFilesAction(
        @Nullable String text,
        @Nullable String description,
        @Nullable Icon icon
    ) {
        super(text, description, icon);
    }

    @Override
    protected boolean isEnabled(
        AnActionEvent e,
        Project project,
        AnalysisStatus status
    ) {
        // log.info("--- SonarAnalyzeAllFilesAction.isEnabled");
        return !status.isRunning() && hasFiles(project);
    }

    @Override
    protected boolean isVisible(String place) {
        return !ActionPlaces.PROJECT_VIEW_POPUP.equals(place);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        log.info("--- SonarAnalyzeAllFilesAction.actionPerformed");
        var project = e.getProject();
        if (project == null || ActionPlaces.PROJECT_VIEW_POPUP.equals(e.getPlace()) || !showWarning()) {
            return;
        }
        SonarLintUtils.getService(project, AnalysisSubmitter.class).analyzeAllFiles();
    }

    static boolean showWarning() {
        if (!ApplicationManager.getApplication().isUnitTestMode() &&
            !PropertiesComponent.getInstance().getBoolean(HIDE_WARNING_PROPERTY, false)) {
            // keep this deprecated call for a while as there is a breaking change between 2020.3 and 2021.3
            var result = Messages.showYesNoDialog(
                "Analysing all files may take a considerable amount of time to complete.\n" +
                "To get the best from SonarLint, you should preferably use the automatic " +
                "analysis of the file you're working on.",
                "SonarLint - Analyze All Files",
                "Proceed", "Cancel", Messages.getWarningIcon(), new DoNotShowAgain()
            );
            return result == Messages.OK;
        }
        return true;
    }

    // Don't use DialogWrapper.DoNotAskOption.Adapter because it's not implemented in older versions of intellij
    static class DoNotShowAgain implements DialogWrapper.DoNotAskOption {

        @Override public boolean isToBeShown() {
            return true;
        }

        @Override public void setToBeShown(boolean toBeShown, int exitCode) {
            PropertiesComponent.getInstance().setValue(HIDE_WARNING_PROPERTY, Boolean.toString(!toBeShown));
        }

        @Override public boolean canBeHidden() {
            return true;
        }

        @Override public boolean shouldSaveOptionsOnCancel() {
            return false;
        }

        @NotNull @Override public String getDoNotShowMessage() {
            return "Don't show again";
        }
    }
}
