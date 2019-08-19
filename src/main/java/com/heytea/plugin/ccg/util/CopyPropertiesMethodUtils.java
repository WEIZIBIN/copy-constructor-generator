package com.heytea.plugin.ccg.util;

import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTypesUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author weizibin
 * @since 2019-08-19 14:47
 */
public class CopyPropertiesMethodUtils {

    private CopyPropertiesMethodUtils() {}

    public static final String METHOD_NAME = "copyPropertiesFrom";

    public static void generateCopyMethod(PsiClass sourceClass, PsiClass targetClass, Project project) {
        PsiElementFactory factory = JavaPsiFacade.getInstance(project).getElementFactory();
        if (alreadyExistCopyMethod(sourceClass, targetClass, factory)) {
            return;
        }

        List<PsiField> sourceFieldList = ConstructorUtils.getAllCopyableFields(sourceClass);
        List<PsiField> targetFieldList = ConstructorUtils.getAllCopyableFields(targetClass);

        Map<String, PsiField> targetFieldNameMap = targetFieldList.stream()
                .collect(Collectors.toMap(PsiField::getName, Function.identity()));

        List<String> copyFieldList = new ArrayList<>();

        // 找出并集
        for (PsiField sourceField : sourceFieldList) {
            String fieldName = sourceField.getName();
            PsiField targetField = targetFieldNameMap.get(fieldName);
            if (targetField == null) {
                continue;
            }
            if (!targetField.getType().isAssignableFrom(sourceField.getType())) {
                continue;
            }
            copyFieldList.add(fieldName);
        }

        String code = generateCopyMethod(sourceClass, copyFieldList);

        PsiMethod methodFromText = factory.createMethodFromText(code, targetClass);

        targetClass.add(methodFromText);
        JavaCodeStyleManager.getInstance(project).shortenClassReferences(targetClass);
    }

    private static boolean alreadyExistCopyMethod(PsiClass sourceClass, PsiClass targetClass, PsiElementFactory elementFactory) {
        PsiMethod[] allMethods = targetClass.getAllMethods();
        for (PsiMethod method : allMethods) {
            if (!Objects.equals(method.getName(), METHOD_NAME)) {
                continue;
            }
            if (method.getParameterList().getParametersCount() != 1) {
                continue;
            }
            PsiParameter param = method.getParameterList().getParameters()[0];
            PsiClass paramClass = PsiTypesUtil.getPsiClass(param.getType());
            if (!PsiClassUtils.isAssignFrom(sourceClass, paramClass, elementFactory)) {
                continue;
            }
            return true;
        }
        return false;
    }

    private static String generateCopyMethod(PsiClass sourceClass, List<String> copyFieldNameList) {
        String parameterName = "other";
        StringBuilder code = new StringBuilder();
        code.append(String.format("public void %s(%s %s) {", METHOD_NAME, sourceClass.getQualifiedName(), parameterName));
        for (String fieldName : copyFieldNameList) {
            code.append(String.format("this.%s = %s.get%s();", fieldName, parameterName, StringUtils.capitalize(fieldName)));
        }
        code.append("}");
        return code.toString();
    }
}
