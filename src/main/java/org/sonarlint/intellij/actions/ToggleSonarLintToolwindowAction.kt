package org.sonarlint.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.actionSystem.ex.ActionButtonLook
import com.intellij.openapi.actionSystem.ex.CustomComponentAction
import com.intellij.openapi.actionSystem.impl.ActionButton
import com.intellij.openapi.actionSystem.impl.ActionButtonWithText
import com.intellij.openapi.project.DumbAwareToggleAction
import com.intellij.openapi.wm.ToolWindow
import com.intellij.util.ui.JBUI
import org.sonarlint.intellij.SonarLintIcons
import org.sonarlint.intellij.ui.SonarLintToolWindowFactory
import javax.swing.JComponent

class ToggleSonarLintToolwindowAction : DumbAwareToggleAction(), CustomComponentAction {
    override fun createCustomComponent(presentation: Presentation, place: String): JComponent =
        object : ActionButtonWithText(this, presentation, place, JBUI.emptySize()) {
            override fun getPopState(): Int = if (myRollover && isEnabled) POPPED else NORMAL
        }.also {
            it.setLook(ActionButtonLook.INPLACE_LOOK)
            it.border = JBUI.Borders.empty(1, 2)
        }

    override fun isSelected(e: AnActionEvent): Boolean {
        return e.applyToToolWindow { isVisible } ?: true
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        e.applyToToolWindow {
            if (state) {
                show()
            } else {
                hide()
            }
        }
    }

    override fun update(e: AnActionEvent) {
        super.update(e)
        val presentation = e.presentation
        if (!isSelected(e)) {
            presentation.text = "6"
            presentation.icon = SonarLintIcons.SONARLINT_ACTION
        } else {
            presentation.text = "6"
            presentation.icon = SonarLintIcons.SONARLINT_ACTION
        }
    }

    private fun <T> AnActionEvent.applyToToolWindow(action: ToolWindow.() -> T) =
        SonarLintToolWindowFactory.getSonarLintToolWindow(project)?.run(action)
}