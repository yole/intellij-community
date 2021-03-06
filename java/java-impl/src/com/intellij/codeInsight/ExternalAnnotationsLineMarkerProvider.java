/*
 * Copyright 2000-2014 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.codeInsight;

import com.intellij.codeHighlighting.Pass;
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler;
import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInsight.intention.IntentionManager;
import com.intellij.codeInsight.javadoc.JavaDocInfoGenerator;
import com.intellij.icons.AllIcons;
import com.intellij.ide.actions.ApplyIntentionAction;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.actionSystem.impl.SimpleDataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtilCore;
import com.intellij.util.Function;
import com.intellij.xml.util.XmlStringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExternalAnnotationsLineMarkerProvider implements LineMarkerProvider {
  @Nullable
  @Override
  public LineMarkerInfo getLineMarkerInfo(@NotNull PsiElement element) {
    return null;
  }

  @Override
  public void collectSlowLineMarkers(@NotNull List<PsiElement> elements, @NotNull Collection<LineMarkerInfo> result) {
    final Set<PsiModifierListOwner> owners = new HashSet<PsiModifierListOwner>();
    for (PsiElement element : elements) {
      if (element instanceof PsiParameter) continue;

      if (element instanceof PsiModifierListOwner) {
        final PsiModifierListOwner modifierListOwner = (PsiModifierListOwner)element;
        final ExternalAnnotationsManager annotationsManager = ExternalAnnotationsManager.getInstance(modifierListOwner.getProject());
        PsiAnnotation[] externalAnnotations = annotationsManager.findExternalAnnotations(modifierListOwner);
        if (externalAnnotations != null && externalAnnotations.length > 0) {
          owners.add((PsiModifierListOwner)element);
        } else if (element instanceof PsiMethod) {
          final PsiParameter[] parameters = ((PsiMethod)element).getParameterList().getParameters();
          for (PsiParameter parameter : parameters) {
            externalAnnotations = annotationsManager.findExternalAnnotations(parameter);
            if (externalAnnotations != null && externalAnnotations.length > 0) {
              owners.add((PsiMethod)element);
              break;
            }
          }
        }
      }
    }

    for (PsiModifierListOwner modifierListOwner : owners) {
      final Function<PsiModifierListOwner, String> annotationsCollector = new Function<PsiModifierListOwner, String>() {
        @Override
        public String fun(PsiModifierListOwner owner) {
          return XmlStringUtil.wrapInHtml(JavaDocInfoGenerator.generateSignature(owner));
        }
      };
      result.add(new LineMarkerInfo<PsiModifierListOwner>(modifierListOwner, modifierListOwner.getTextOffset(), AllIcons.Nodes.Annotationtype, 
                                                          Pass.UPDATE_OVERRIDEN_MARKERS,
                                                          annotationsCollector, new MyIconGutterHandler(),
                                                          GutterIconRenderer.Alignment.LEFT));
    }
  }

  private static class MyIconGutterHandler implements GutterIconNavigationHandler<PsiModifierListOwner> {
    @Override
    public void navigate(MouseEvent e, final PsiModifierListOwner listOwner) {
      final PsiFile containingFile = listOwner.getContainingFile();
      final VirtualFile virtualFile = PsiUtilCore.getVirtualFile(listOwner);
      if (virtualFile != null && containingFile != null) {
        final Project project = listOwner.getProject();
        final OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, virtualFile, listOwner.getTextOffset());
        final Editor editor = FileEditorManager.getInstance(project).openTextEditor(openFileDescriptor, true);
        if (editor != null) {
          final DefaultActionGroup group = new DefaultActionGroup();
          for (final IntentionAction action : IntentionManager.getInstance().getAvailableIntentionActions()) {
            if (action.isAvailable(project, editor, containingFile)) {
              group.add(new ApplyIntentionAction(action, action.getText(), editor, containingFile));
            }
          }
          if (group.getChildrenCount() > 0) {
            editor.getScrollingModel().runActionOnScrollingFinished(new Runnable() {
              @Override
              public void run() {
                JBPopupFactory.getInstance()
                  .createActionGroupPopup(null, group, SimpleDataContext.getProjectContext(null),
                                          JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true)
                  .showInBestPositionFor(editor);
              }
            });
          }
        }
      }
    }
  }
}
