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
package org.sonarlint.intellij.analysis.cayc

import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.changes.ChangeList
import com.intellij.openapi.vcs.changes.ChangeListListener
import com.intellij.openapi.vcs.changes.ChangeListManagerEx
import org.sonarlint.intellij.analysis.AnalysisSubmitter
import org.sonarlint.intellij.common.util.SonarLintUtils
import org.sonarlint.intellij.trigger.TriggerType

class CleanAsYouCodeManager(private val project: Project) {
    private var currentScope = DEFAULT_SCOPE

    fun getCurrentScopedDiff(): VcsDiff {
        return currentScope.getDiff(project)
    }

    fun initialize() {
        // we should trigger a new analysis:
        // at startup, on all files changed since last commit (staged, tracked, untracked)
        // when the user edits a not-ignored file (optimization: if was changed and backed to unchanged, no need to re-analyze, just update the view)
        // when the VFS changes, on new eligible files that appeared
        // when the changelist changes

        // we should update the list of findings:
        // after the analysis is completed
        // when the user commits -> remove findings for files that have been committed


        val instance = ChangeListManagerEx.getInstance(project)
        instance.addChangeListListener(object : ChangeListListener {
            override fun changeListChanged(list: ChangeList?) {

            }
            override fun changeListUpdateDone() {
                //analyzeChangedFiles()
            }
        })
        analyzeChangedFiles()
    }

    private fun analyzeChangedFiles() {
        SonarLintUtils.getService(project, AnalysisSubmitter::class.java).autoAnalyzeChangedFiles(
            getCurrentScopedDiff().filesInvolved(),
            TriggerType.CHANGED_FILES
        )
    }

    companion object {
        private val DEFAULT_SCOPE = Scope.SINCE_LAST_COMMIT
    }
}