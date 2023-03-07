package org.sonarlint.intellij.analysis.cayc

import com.intellij.openapi.vfs.VirtualFile
import org.sonarsource.sonarlint.core.commons.TextRange

data class VcsDiff(val fileDiffs: List<VcsFileDiff>) {
    fun filesInvolved() = fileDiffs.map { it.file }.toSet()
}

data class VcsFileDiff(val file: VirtualFile, val changedRanges: List<TextRange>)