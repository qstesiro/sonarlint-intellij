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
import com.intellij.openapi.vcs.changes.ChangeListManager
import git4idea.changes.GitChangeUtils
import java.util.function.Function

// files changed since last commit are:
//  - files staged/added.
//  - files tracked and not staged
// ^ both corresponds to affectedFiles
// untracked files (new, not added)
// we ignore ranges for the moment

// the default (or active) change list should be the starting point, to ignore changes in other change lists
// warning: some lines of a file could be part of a changelist and some others in another changelist
private fun getChangesSinceLastCommit(project: Project) : VcsDiff {
    return with(ChangeListManager.getInstance(project)) {
        VcsDiff((defaultChangeList.changes.mapNotNull { it.virtualFile }.toSet() + unversionedFilesPaths.mapNotNull { it.virtualFile })
            .map { VcsFileDiff(it, emptyList()) })
    }
}
private fun getChangesSinceLastPush(project: Project) : VcsDiff {
    return GitChangeUtils.parseChangeList()
}

enum class Scope(private val affectedFilesSupplier: Function<Project, VcsDiff>) {
    SINCE_LAST_COMMIT(::getChangesSinceLastCommit),
    SINCE_LAST_PUSH(::getChangesSinceLastPush);

    fun getDiff(project: Project): VcsDiff {
        return affectedFilesSupplier.apply(project)
    }
}
