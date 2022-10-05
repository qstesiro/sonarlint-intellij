package org.sonarlint.intellij.sonarsecurity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.signature.SignatureWriter;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class SonarSecurityAnnotator implements Annotator {


  @Override
  public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
    // Ensure the Psi Element is an expression
    if (!(element instanceof PsiMethod)) {
      return;
    }

    PsiMethod method = (PsiMethod) element;


    PsiClass containingClass = method.getContainingClass();

    String qualifiedName = containingClass.getQualifiedName();

    String methodAsmSignature = MapPsiToAsmDesc.INSTANCE.methodDesc(method);


    if (method.getName().contains("sink")) {
      holder.newAnnotation(HighlightSeverity.WARNING, "This is a sink!!! " + qualifiedName + "#" + method.getName() + methodAsmSignature)
              .range(method.getNameIdentifier().getTextRange())
              .textAttributes(CodeInsightColors.WARNINGS_ATTRIBUTES)
              .highlightType(ProblemHighlightType.WEAK_WARNING)
              .create();
    }
    if (method.getName().contains("source")) {
      holder.newAnnotation(HighlightSeverity.WARNING, "This is a source!!!" + qualifiedName)
              .range(method.getNameIdentifier().getTextRange())
              .textAttributes(CodeInsightColors.WARNINGS_ATTRIBUTES)
              .highlightType(ProblemHighlightType.WEAK_WARNING)
              .create();
    }
  }

}