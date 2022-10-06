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
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.ClassUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
        if ((element instanceof PsiMethod)) {
            PsiMethod method = (PsiMethod) element;
            processingMethodId(holder, toMethodId(method), method.getNameIdentifier().getTextRange());
        } else if ((element instanceof PsiCall)) {
            PsiCall methodCall = (PsiCall) element;
            processingMethodId(holder, toMethodId(methodCall), methodCall.getTextRange());
        }
    }

    private void processingMethodId(@NotNull AnnotationHolder holder, @Nullable String methodId, TextRange range) {
        if (methodId == null) {
            return;
        }
        rulesConfig.forEach((key, config) -> {
            handleMethodConfig(config.sinks, methodId, holder, "sink", key, range);
            handleMethodConfig(config.sources, methodId, holder, "source", key, range);
            handleMethodConfig(config.sanitizers, methodId, holder, "sanitizer", key, range);
            handleMethodConfig(config.passthroughs, methodId, holder, "passthrough", key, range);
            handleMethodConfig(config.validators, methodId, holder, "validator", key, range);
        });
    }

    private void handleMethodConfig(JsonConfigDeserializer.MethodConfig[] configs, String psiMethodId, AnnotationHolder holder, String configType, String ruleKey, TextRange range) {
        if (configs != null) {
            for (JsonConfigDeserializer.MethodConfig config : configs) {
                if ((config.isMethodPrefix && psiMethodId.startsWith(config.methodId))
                        || config.methodId.equals(psiMethodId)) {
                    createAnnotation(holder, range, configType, ruleKey);
                }
            }
        }

    }


    private static void createAnnotation(@NotNull AnnotationHolder holder, TextRange range, String type, String ruleKey) {
        holder.newAnnotation(HighlightSeverity.WARNING, "This is a " + type + " for " + ruleKey)
                .range(range)
                .textAttributes(CodeInsightColors.WARNINGS_ATTRIBUTES)
                .highlightType(ProblemHighlightType.WEAK_WARNING)
                .create();
    }

    private String toMethodId(PsiMethod psiMethod) {
        String asmSignature = ClassUtil.getAsmMethodSignature(psiMethod);

        PsiClass containingClass = psiMethod.getContainingClass();
        String qualifiedName = containingClass.getQualifiedName();
        String methodName = psiMethod.isConstructor() ? "<init>" : psiMethod.getName();
        return qualifiedName + "#" + methodName + asmSignature;
    }

    @Nullable
    private String toMethodId(PsiCall psiCall) {
        var psiMethod = psiCall.resolveMethod();
        // Can be null for default constructors
        if (psiMethod != null) {
            return toMethodId(psiMethod);
        }
        return null;
    }


}