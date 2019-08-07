package com.heytea.plugin.ccg.action;

import com.heytea.plugin.ccg.handler.GenerateCopyConstructorHandler;
import com.heytea.plugin.ccg.util.ConstructorUtil;
import com.intellij.codeInsight.generation.actions.BaseGenerateAction;
import com.intellij.psi.PsiAnonymousClass;
import com.intellij.psi.PsiClass;

/**
 * @author weizibin
 * @since 2019-08-07 13:38
 */
public class GenerateCopyConstructorAction extends BaseGenerateAction {
    public GenerateCopyConstructorAction() {
        super(new GenerateCopyConstructorHandler());
    }

    @Override
    protected boolean isValidForClass(PsiClass targetClass) {
        return super.isValidForClass(targetClass) && !(targetClass instanceof PsiAnonymousClass)
            && !ConstructorUtil.hasCopyConstructor(targetClass);
    }
}
