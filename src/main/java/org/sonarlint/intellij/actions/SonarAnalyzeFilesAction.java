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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.project.ProjectCoreUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.openapi.diagnostic.Logger;
import icons.SonarLintIcons;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonarlint.intellij.analysis.AnalysisStatus;
import org.sonarlint.intellij.analysis.AnalysisSubmitter;
import org.sonarlint.intellij.ui.SonarLintToolWindowFactory;

import static org.sonarlint.intellij.common.util.SonarLintUtils.getService;

public class SonarAnalyzeFilesAction extends DumbAwareAction {

    static final Logger log = Logger.getInstance(SonarAnalyzeFilesAction.class);

    public SonarAnalyzeFilesAction() {
        super();
    }

    public SonarAnalyzeFilesAction(@Nullable String text, @Nullable String description, @Nullable Icon icon) {
        super(text, description, icon);
        log.info("--- SonarAnalyzeFilesAction.SonarAnalyzeFilesAction");
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
        var files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (files == null || files.length == 0 || AbstractSonarAction.isRiderSlnOrCsproj(files)) {
            e.getPresentation().setEnabled(false);
            e.getPresentation().setVisible(false);
            return;
        }
        if (SonarLintToolWindowFactory.TOOL_WINDOW_ID.equals(e.getPlace())) {
            e.getPresentation().setIcon(SonarLintIcons.PLAY);
        }
        e.getPresentation().setVisible(true);
        var project = e.getProject();
        if (project == null || !project.isInitialized() || project.isDisposed()) {
            e.getPresentation().setEnabled(false);
            return;
        }
        var status = getService(project, AnalysisStatus.class);
        if (status.isRunning()) {
            e.getPresentation().setEnabled(false);
            return;
        }
        e.getPresentation().setEnabled(true);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        var project = e.getProject();
        var files = e.getData(CommonDataKeys.VIRTUAL_FILE_ARRAY);
        if (project == null || project.isDisposed() || files == null || files.length == 0) {
            return;
        }
        var hasProject = Stream.of(files)
            .anyMatch(f -> f.getPath().equals(project.getBasePath()));
        if (hasProject && !SonarAnalyzeAllFilesAction.showWarning()) {
            return;
        }
        var fileSet = Stream.of(files)
            .flatMap(
                f -> {
                    if (f.isDirectory()) {
                        var visitor = new CollectFilesVisitor();
                        VfsUtilCore.visitChildrenRecursively(f, visitor);
                        return visitor.files.stream();
                    } else {
                        return Stream.of(f);
                    }
                }
            )
            .collect(Collectors.toSet());
        getService(project, AnalysisSubmitter.class).analyzeFilesOnUserAction(fileSet, e);
    }

    private static class CollectFilesVisitor extends VirtualFileVisitor {

        private final Set<VirtualFile> files = new LinkedHashSet<>();

        public CollectFilesVisitor() {
            super(VirtualFileVisitor.NO_FOLLOW_SYMLINKS);
        }

        @Override
        public boolean visitFile(@NotNull VirtualFile file) {
            var projectFile = ProjectCoreUtil.isProjectOrWorkspaceFile(file, file.getFileType());
            if (!file.isDirectory() && !file.getFileType().isBinary() && !projectFile) {
                files.add(file);
            }
            return !projectFile && !".git".equals(file.getName());
        }
    }
}
