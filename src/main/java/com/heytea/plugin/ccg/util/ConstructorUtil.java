package com.heytea.plugin.ccg.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiCodeBlock;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import com.intellij.psi.PsiParameterList;
import com.intellij.psi.PsiStatement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author weizibin
 * @since 2019-08-07 14:05
 */
public class ConstructorUtil {
    private ConstructorUtil() {}

    public static boolean isCopyConstructor(@NotNull PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        if (method.isConstructor() && containingClass != null) {
            String className = containingClass.getQualifiedName();
            PsiParameterList paramList = method.getParameterList();
            return paramList.getParametersCount() == 1
                && paramList.getParameters()[0].getType().getCanonicalText().equals(className);
        } else {
            return false;
        }
    }

    @Nullable
    public static PsiMethod findCopyConstructor(@Nullable PsiClass psiClass) {
        if (psiClass == null) {
            return null;
        } else {
            PsiMethod[] constructors = psiClass.getConstructors();

            for (PsiMethod constructor : constructors) {
                if (isCopyConstructor(constructor)) {
                    return constructor;
                }
            }

            return null;
        }
    }

    public static boolean hasCopyConstructor(@Nullable PsiClass psiClass) {
        return findCopyConstructor(psiClass) != null;
    }

    @Nullable
    public static PsiMethod findConstructorCall(PsiMethod constructor) {
        PsiCodeBlock body = constructor.getBody();
        if (body != null) {
            PsiStatement[] statements = body.getStatements();
            if (statements.length != 0) {
                PsiElement firstChild = statements[0].getFirstChild();
                if (firstChild instanceof PsiMethodCallExpression) {
                    PsiMethodCallExpression callExpression = (PsiMethodCallExpression)firstChild;
                    PsiMethod methodCallTarget = callExpression.resolveMethod();
                    if (methodCallTarget != null && methodCallTarget.isConstructor()) {
                        return methodCallTarget;
                    }
                }
            }
        }

        return null;
    }

    public static List<PsiField> getAllCopyableFields(PsiClass psiClass) {
        List<PsiField> copyableFields = new ArrayList<>();
        PsiField[] fields = psiClass.getFields();

        for (PsiField field : fields) {
            if (isCopyableField(field)) {
                copyableFields.add(field);
            }
        }

        return copyableFields;
    }

    private static boolean isCopyableField(PsiField field) {
        return !field.hasModifierProperty(PsiModifier.STATIC)
            && (!field.hasModifierProperty(PsiModifier.FINAL) || field.getInitializer() == null);
    }
}
