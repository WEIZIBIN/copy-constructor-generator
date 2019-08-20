package com.heytea.plugin.ccg.inspection;

import com.heytea.plugin.ccg.util.CopyPropertiesMethodUtils;
import com.intellij.codeInspection.AbstractBaseJavaLocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiExpressionList;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiType;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author weizibin
 * @since 2019-08-19 14:00
 */
public class BeanUtilsCopyPropertiesInspection extends AbstractBaseJavaLocalInspectionTool {
    private static final Logger LOG = Logger.getInstance(BeanUtilsCopyPropertiesInspection.class);
    private final QuickFix quickFix = new QuickFix();

    public static final String QUICK_FIX_NAME =
            "Use copy-properties-method instead of using BeanUtils.copyProperties";

    @NonNls
    public static final String CHECKED_METHOD = "org.springframework.beans.BeanUtils.copyProperties";

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
        return new JavaElementVisitor() {

            @NonNls
            private final String DESCRIPTION =
                    "BeanUtils copy properties is danger";

            @Override
            public void visitMethodCallExpression(PsiMethodCallExpression expression) {
                super.visitMethodCallExpression(expression);
                PsiMethod method = expression.resolveMethod();
                if (method == null) {
                    return;
                }

                if (isCheckedMethod(method) && isCheckedArgument(expression.getArgumentList())) {
                    // Identified an expression with potential problems, add to list with fix object.
                    holder.registerProblem(expression, DESCRIPTION, quickFix);
                }
            }

            private boolean isCheckedArgument(PsiExpressionList argumentList) {
                if (argumentList.getExpressionCount() != 2) {
                    return false;
                }

                // assignable class copy not warming
                PsiType sourceArgumentType = argumentList.getExpressionTypes()[0];
                PsiType targetArgumentType = argumentList.getExpressionTypes()[1];
                if (sourceArgumentType.isAssignableFrom(targetArgumentType)
                        || targetArgumentType.isAssignableFrom(sourceArgumentType)) {
                    return false;
                }

                return true;
            }

            private boolean isCheckedMethod(@NotNull PsiMethod method) {
                String memberQualifiedName = PsiUtil.getMemberQualifiedName(method);
                return Objects.equals(CHECKED_METHOD, memberQualifiedName);
            }

        };
    }

    private static class QuickFix implements LocalQuickFix {

        @NotNull
        @Override
        public String getName() {
            return QUICK_FIX_NAME;
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement psiElement = descriptor.getPsiElement();
            if (!(psiElement instanceof PsiMethodCallExpression)) {
                return;
            }

            PsiMethodCallExpression expression = (PsiMethodCallExpression) psiElement;
            PsiMethod method = expression.resolveMethod();
            if (method == null) {
                return;
            }

            PsiClass sourceArgClass = PsiTypesUtil.getPsiClass(expression.getArgumentList().getExpressionTypes()[0]);
            PsiClass targetArgClass = PsiTypesUtil.getPsiClass(expression.getArgumentList().getExpressionTypes()[1]);

            String sourceArgName = expression.getArgumentList().getExpressions()[0].getText();
            String targetArgName = expression.getArgumentList().getExpressions()[1].getText();

            CopyPropertiesMethodUtils.generateCopyMethod(sourceArgClass, targetArgClass, project);

            PsiElementFactory elementFactory = JavaPsiFacade.getInstance(project).getElementFactory();
            PsiExpression expressionFromText = elementFactory.createExpressionFromText(
                    String.format("%s.%s(%s)", targetArgName, CopyPropertiesMethodUtils.METHOD_NAME, sourceArgName), null);
            expression.replace(expressionFromText);
        }

        @Override
        @NotNull
        public String getFamilyName() {
            return getName();
        }
    }
}