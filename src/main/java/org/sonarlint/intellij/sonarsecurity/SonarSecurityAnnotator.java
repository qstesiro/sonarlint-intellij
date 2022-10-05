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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Map;

import static com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey;

public class SonarSecurityAnnotator implements Annotator {

  static Map<String, JsonConfigDeserializer.RuleConfig> rulesConfig;

  static {
    var is = SonarSecurityAnnotator.class.getResourceAsStream("/sonarsecurityconfig.json");
    rulesConfig = new JsonConfigDeserializer().deserialize(new InputStreamReader(is));
  }



  @Override
  public void annotate(@NotNull final PsiElement element, @NotNull AnnotationHolder holder) {
    // Ensure the Psi Element is an expression
    if (!(element instanceof PsiMethod)) {
      return;
    }

    PsiMethod method = (PsiMethod) element;

    String methodId = toMethodId(method);

    rulesConfig.forEach((key, config) -> {
      handleMethodConfig(config.sinks, methodId, holder, "sink", key, method);
      handleMethodConfig(config.sources, methodId, holder, "source", key, method);
      handleMethodConfig(config.sanitizers, methodId, holder, "sanitizer", key, method);
      handleMethodConfig(config.passthroughs, methodId, holder, "passthrough", key, method);
      handleMethodConfig(config.validators, methodId, holder, "validator", key, method);
    });

  }

  private void handleMethodConfig(JsonConfigDeserializer.MethodConfig[] configs, String psiMethodId, AnnotationHolder holder, String configType, String ruleKey, PsiMethod method) {
    if (configs != null) {
      for (JsonConfigDeserializer.MethodConfig config : configs) {
        if (config.methodId.equals(psiMethodId)) {
          createAnnotation(holder, method, configType, ruleKey);
        }
      }
    }

  }


  private static void createAnnotation(@NotNull AnnotationHolder holder, PsiMethod method, String type, String ruleKey) {
    holder.newAnnotation(HighlightSeverity.WARNING, "This is a " + type + " for " + ruleKey)
            .range(method.getNameIdentifier().getTextRange())
            .textAttributes(CodeInsightColors.WARNINGS_ATTRIBUTES)
            .highlightType(ProblemHighlightType.WEAK_WARNING)
            .create();
  }

  private String toMethodId(PsiMethod psiMethod) {
      PsiClass containingClass = psiMethod.getContainingClass();
      String qualifiedName = containingClass.getQualifiedName();
      String methodAsmSignature = MapPsiToAsmDesc.INSTANCE.methodDesc(psiMethod);
      return qualifiedName + "#" + psiMethod.getName() + methodAsmSignature;
    }

}