// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.compiler.backwardRefs.scala

import com.intellij.compiler.backwardRefs.CompilerReferenceReaderFactory
import com.intellij.compiler.server.BuildManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import org.jetbrains.jps.backwardRefs.BackwardReferenceIndexDescriptor
import org.jetbrains.jps.backwardRefs.index.CompilerReferenceIndexUtil

class ScalaCompilerReferenceReaderFactory extends CompilerReferenceReaderFactory[ScalaCompilerReferenceReader] {
  import ScalaCompilerReferenceReaderFactory._

  override def create(project: Project): ScalaCompilerReferenceReader = {
    val buildDir = BuildManager.getInstance.getProjectSystemDirectory(project)
    if (!CompilerReferenceIndexUtil.existsUpToDate(buildDir, BackwardReferenceIndexDescriptor.INSTANCE)) null
    else {
      try new ScalaCompilerReferenceReader(BuildManager.getInstance.getProjectSystemDirectory(project))
      catch {
        case e: RuntimeException =>
          LOG.error("An exception while initialization of compiler reference index.", e)
          null
      }
    }
  }
}

object ScalaCompilerReferenceReaderFactory {
  private val LOG = Logger.getInstance(classOf[ScalaCompilerReferenceReaderFactory])
}
