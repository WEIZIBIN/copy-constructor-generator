package com.heytea.plugin.ccg.handler;

import com.heytea.plugin.ccg.util.ConstructorUtil;
import com.intellij.codeInsight.generation.ClassMember;
import com.intellij.codeInsight.generation.GenerateMembersHandlerBase;
import com.intellij.codeInsight.generation.GenerationInfo;
import com.intellij.codeInsight.generation.PsiFieldMember;
import com.intellij.codeInsight.generation.PsiGenerationInfo;
import com.intellij.ide.util.TreeClassChooser;
import com.intellij.ide.util.TreeClassChooserFactory;
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
 * @since 2019-08-07 14:05
 */
public class GenerateCopyConstructorHandler extends GenerateMembersHandlerBase {

    private PsiClass target;
    private PsiClass source;
    private Project project;

    public GenerateCopyConstructorHandler() {
        super(null);
    }

    @Override
    protected String getNothingFoundMessage() {
        return "Copy constructor already exists";
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
                .createAllProjectScopeChooser("Choose Source Class");
        dialog.showDialog();
        this.source = dialog.getSelected();
        this.target = aClass;
        this.project = project;

        if (source == null) {
            return null;
        }

        return getAllOriginalMembers(aClass);
    }

    @Override
    @NotNull
    protected List<? extends GenerationInfo> generateMemberPrototypes(PsiClass aClass, ClassMember[] members)
        throws IncorrectOperationException {
        PsiMethod copyConstructor = this.generateCopyConstructor(aClass, members);
        return Collections.singletonList(new PsiGenerationInfo<>(copyConstructor));
    }

    @Override
    protected GenerationInfo[] generateMemberPrototypes(PsiClass aClass, ClassMember originalMember)
        throws IncorrectOperationException {
        return null;
    }

    @Override
    protected void cleanup() {
        super.cleanup();
        Optional.ofNullable(project)
                .ifPresent(project -> JavaCodeStyleManager.getInstance(project).shortenClassReferences(target));
        project = null;
        target = null;
        source = null;
    }

    private PsiMethod generateCopyConstructor(PsiClass psiClass, ClassMember[] copyableFields) {
        String sourceClassName = source.getName();
        String targetClassCapitalizeName = StringUtils.capitalize(target.getName());
        String sourceClassQualifiedName = source.getQualifiedName();

        String parameterName = StringUtils.uncapitalize(sourceClassName);
        StringBuilder code = new StringBuilder();
        code.append(String.format("public %s(%s %s) {", targetClassCapitalizeName, sourceClassQualifiedName, parameterName));

        for (ClassMember fieldMember : copyableFields) {
            PsiField field = ((PsiFieldMember)fieldMember).getElement();
            String name = field.getName();
            code.append(String.format("this.%s = %s.get%s();", name, parameterName, StringUtils.capitalize(name)));
        }

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
