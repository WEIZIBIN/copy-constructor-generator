package com.heytea.plugin.ccg.handler;

import com.heytea.plugin.ccg.util.ConstructorUtil;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.GenerateMembersHandlerBase;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.util.IncorrectOperationException;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author weizibin
 */
public class GenerateConvertMethodHandler extends GenerateMembersHandlerBase {

    private PsiClass target;
    private PsiClass source;

    public GenerateConvertMethodHandler() {
        super(null);
    }

    @Override
    protected ClassMember[] getAllOriginalMembers(PsiClass aClass) {
        return this.toMembers(ConstructorUtil.getAllCopyableFields(aClass));
    }

    @Nullable
    @Override
    protected ClassMember[] chooseOriginalMembers(PsiClass aClass, Project project) {
        // select target class
        TreeClassChooser dialog = TreeClassChooserFactory.getInstance(project)
                .createAllProjectScopeChooser("Choose Target Class");
        dialog.showDialog();
        target = dialog.getSelected();
        source = aClass;

        if (target == null) {
            return null;
        }

        return getAllOriginalMembers(aClass);
    }

    @Override
    @NotNull
    protected List<? extends GenerationInfo> generateMemberPrototypes(PsiClass aClass, ClassMember[] members)
        throws IncorrectOperationException {
        PsiMethod method = this.generateConvertMethod(aClass, members);
        return Collections.singletonList(new PsiGenerationInfo<>(method));
    }

    @Override
    protected GenerationInfo[] generateMemberPrototypes(PsiClass aClass, ClassMember originalMember)
        throws IncorrectOperationException {
        throw new IncorrectOperationException();
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        target = null;
        source = null;
    }

    @Override
    protected void notifyOnSuccess(Editor editor, ClassMember[] members, List<? extends GenerationInfo> generatedMembers) {
        super.notifyOnSuccess(editor, members, generatedMembers);
        Optional.ofNullable(editor.getProject())
                .ifPresent(project -> JavaCodeStyleManager.getInstance(project).shortenClassReferences(source));
    }

    private PsiMethod generateConvertMethod(PsiClass psiClass, ClassMember[] copyableFields) {
        String targetClassName = target.getName();
        String targetClassCapitalizeName = StringUtils.capitalize(targetClassName);
        String targetClassQualifiedName = target.getQualifiedName();

        String parameterName = StringUtils.uncapitalize(targetClassName);
        StringBuilder code = new StringBuilder();

        code.append(String.format("public %s to%s() {", targetClassQualifiedName, targetClassCapitalizeName));
        code.append(String.format("%s %s = new %s();", targetClassQualifiedName, parameterName, targetClassQualifiedName));
        for (ClassMember fieldMember : copyableFields) {
            PsiField field = ((PsiFieldMember)fieldMember).getElement();
            String name = field.getName();
            code.append(String.format("%s.set%s(this.%s);", parameterName, StringUtils.capitalize(name), name));
        }
        code.append(String.format("return %s;", parameterName));
        code.append("}");

        PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
        return elementFactory.createMethodFromText(code.toString(), psiClass);
    }

    private ClassMember[] toMembers(List<PsiField> allCopyableFields) {
        ClassMember[] classMembers = new ClassMember[allCopyableFields.size()];

        for (int i = 0; i < allCopyableFields.size(); ++i) {
            classMembers[i] = new PsiFieldMember(allCopyableFields.get(i));
        }

        return classMembers;
    }
}
