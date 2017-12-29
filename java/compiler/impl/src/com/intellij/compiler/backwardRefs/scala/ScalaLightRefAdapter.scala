// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.compiler.backwardRefs.scala

import java.util

import com.intellij.compiler.backwardRefs.{JavaLightUsageAdapter, SearchId}
import com.intellij.ide.highlighter.{JavaClassFileType, JavaFileType}
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.PsiFileWithStubSupport

class ScalaLightRefAdapter extends JavaLightUsageAdapter {
  override def getFileTypes: util.Set[FileType] =
    new util.HashSet[FileType](
      util.Arrays.asList(JavaFileType.INSTANCE, JavaClassFileType.INSTANCE /*, ScalaFileType.*/ )
    )

  /**
   * @return classes that can be inheritors of given superClass. This method shouldn't directly check are
   *         found elements really inheritors.
   */
  override def findDirectInheritorCandidatesInFile(
    internalNames: Array[SearchId],
    file: PsiFileWithStubSupport
  ): Array[PsiElement] = Array()

  /**
   * @param indices - ordinal-numbers (corresponding to compiler tree index visitor) of required functional expressions.
   * @return functional expressions for given functional type. Should return
   */
  override def findFunExpressionsInFile(indices: Array[SearchId], file: PsiFileWithStubSupport): Array[PsiElement] =
    Array()
}
