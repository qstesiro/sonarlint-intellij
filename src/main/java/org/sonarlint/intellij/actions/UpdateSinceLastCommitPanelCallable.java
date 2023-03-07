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

import com.intellij.util.ui.UIUtil;
import org.sonarlint.intellij.analysis.AnalysisCallback;
import org.sonarlint.intellij.analysis.AnalysisResult;
import org.sonarlint.intellij.analysis.cayc.CleanAsYouCodeFindingsHolder;

public class UpdateSinceLastCommitPanelCallable implements AnalysisCallback {
  private final CleanAsYouCodeFindingsHolder cleanAsYouCodeFindingsHolder;

  public UpdateSinceLastCommitPanelCallable(CleanAsYouCodeFindingsHolder cleanAsYouCodeFindingsHolder) {
    this.cleanAsYouCodeFindingsHolder = cleanAsYouCodeFindingsHolder;
  }

  @Override public void onError(Throwable e) {
    // nothing to do
  }

  @Override
  public void onSuccess(AnalysisResult analysisResult) {
    showSinceLastCommitTab(analysisResult);
  }

  private void showSinceLastCommitTab(AnalysisResult analysisResult) {
    UIUtil.invokeLaterIfNeeded(() -> cleanAsYouCodeFindingsHolder.updateOnAnalysisResult(analysisResult));
  }
}
