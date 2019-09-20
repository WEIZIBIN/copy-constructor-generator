package com.heytea.plugin.ccg.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiModifier;

import java.util.ArrayList;
import java.util.List;

/**
 * @author weizibin
 * @since 2019-08-07 14:05
 */
public class ConstructorUtil {
    private ConstructorUtil() {}

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
