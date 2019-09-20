package com.heytea.plugin.ccg.action;

import com.heytea.plugin.ccg.handler.GenerateConvertMethodHandler;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;

/**
 * @author weizibin
 */
public class GenerateConvertMethodAction extends BaseGenerateAction {
    public GenerateConvertMethodAction() {
        super(new GenerateConvertMethodHandler());
    }

    @Override
    protected boolean isValidForClass(PsiClass targetClass) {
        return super.isValidForClass(targetClass) && !(targetClass instanceof PsiAnonymousClass);
    }
}
