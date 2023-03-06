package org.sonarlint.intellij.editor

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ex.ActionManagerEx
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.markup.InspectionWidgetActionProvider

private class SonarLintEditorWidgetActionProvider : InspectionWidgetActionProvider {
	override fun createAction(editor: Editor): AnAction {
		return object : DefaultActionGroup(ActionManagerEx.getInstanceEx().getAction("SonarLint.toolwindow.toggle")) {
			override fun update(e: AnActionEvent) {
				e.presentation.isEnabledAndVisible = true
			}
		}
	}
}