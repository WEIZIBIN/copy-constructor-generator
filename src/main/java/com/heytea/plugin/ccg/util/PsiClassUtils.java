package com.heytea.plugin.ccg.util;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import org.jetbrains.annotations.NotNull;

/**
 * @author weizibin
 * @since 2019-08-19 19:32
 */
public class PsiClassUtils {

    public static boolean isAssignFrom(@NotNull PsiClass source, @NotNull PsiClass target, @NotNull PsiElementFactory factory) {
        return factory.createType(source).isAssignableFrom(factory.createType(target));
    }

}
