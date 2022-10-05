package org.sonarlint.intellij.sonarsecurity;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class SonarSecurityAnnotator implements Annotator {


  @Override
  public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
    // Ensure the Psi Element is an expression
    if (!(element instanceof PsiMethod)) {
      return;
    }

    PsiMethod method = (PsiMethod) element;
    if (method.getName().contains("sink")) {
      holder.newAnnotation(HighlightSeverity.WARNING, "This is a sink!!!")
              .range(method.getNameIdentifier().getTextRange())
              .textAttributes(CodeInsightColors.WARNINGS_ATTRIBUTES)
              .highlightType(ProblemHighlightType.WEAK_WARNING)
              .create();
    }
    if (method.getName().contains("source")) {
      holder.newAnnotation(HighlightSeverity.WARNING, "This is a source!!!")
              .range(method.getNameIdentifier().getTextRange())
              .textAttributes(CodeInsightColors.WARNINGS_ATTRIBUTES)
              .highlightType(ProblemHighlightType.WEAK_WARNING)
              .create();
    }
  }

}