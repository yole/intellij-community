/*
 * Copyright 2000-2007 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.lang.psi;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiClass;
import org.jetbrains.plugins.groovy.lang.psi.api.toplevel.imports.GrImportStatement;
import org.jetbrains.plugins.groovy.GroovyFileType;

/**
 * @author ven
 */
public abstract class GroovyElementFactory {

  private static String DUMMY = "dummy.";

  public static GroovyElementFactory getInstance(Project project) {
    return project.getComponent(GroovyElementFactory.class);
  }

  /**
   *
   * @param aClass - class to be imported
   * @param manager - PsiManager
   * @return import statement for given class
   */
  public static GrImportStatement createImportStatementFromText(PsiClass aClass, PsiManager manager) {
    PsiFile dummyFile = manager.getElementFactory().createFileFromText(DUMMY + GroovyFileType.GROOVY_FILE_TYPE.getDefaultExtension(),
            "import " + aClass.getQualifiedName() + " ");
    return ((GrImportStatement) dummyFile.getFirstChild());
  }

  /**
   * Creates white space
   * @param manager given PsiManager
   * @return new line psi element
   */
  public static PsiElement createWhiteSpace(PsiManager manager) {
    PsiFile dummyFile = manager.getElementFactory().createFileFromText(DUMMY + GroovyFileType.GROOVY_FILE_TYPE.getDefaultExtension(),
            " ");
    return dummyFile.getFirstChild();
  }

  public abstract PsiElement createIdentifierFromText(String idText);
}
