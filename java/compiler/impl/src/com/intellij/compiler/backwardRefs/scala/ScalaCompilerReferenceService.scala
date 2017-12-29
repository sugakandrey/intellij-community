// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.compiler.backwardRefs.scala

import java.util
import java.util.function.BiConsumer

import scala.collection.JavaConverters._

import scala.collection.JavaConverters._
import com.intellij.compiler.{CompilerIOUtil, CompilerReferenceService}
import com.intellij.compiler.backwardRefs.CompilerReferenceServiceBase
import com.intellij.compiler.backwardRefs.CompilerReferenceServiceBase.{IndexCloseReason, IndexOpenReason}
import com.intellij.compiler.impl.CompilerUtil
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiDocumentManager
import com.intellij.util.messages.MessageBusConnection

class ScalaCompilerReferenceService(
  project: Project,
  fileDocumentManager: FileDocumentManager,
  psiDocumentManager: PsiDocumentManager
) extends {
  private[this] val callback: BiConsumer[MessageBusConnection, util.Set[String]] = (connection, affectedModules) => {
    connection.subscribe(CompilerReferenceIndexingTopics.indexingStatus, new IndexingStatusListener {
      override def indexingFinished(affectedModuleNames: Set[String]): Unit =
        affectedModules.addAll(affectedModuleNames.asJava)
    })
  }
} with CompilerReferenceServiceBase[ScalaCompilerReferenceReader](
  project,
  fileDocumentManager,
  psiDocumentManager,
  ScalaCompilerReferenceReaderFactory,
  callback
) {
  override def projectOpened(): Unit = if (CompilerReferenceService.isEnabled) {
    val connection = project.getMessageBus.connect(project)

    connection.subscribe(CompilerReferenceIndexingTopics.indexingStatus, new IndexingStatusListener {
      override def indexingStarted(): Unit = closeReaderIfNeed(IndexCloseReason.COMPILATION_STARTED)
      
      override def indexingFinished(affectedModules: Set[String]): Unit =
        openReaderIfNeed(IndexOpenReason.COMPILATION_FINISHED)
    })

    ReadAction.run(CompilerUtil.refreshOutputRoots())
    myDirtyScopeHolder.installVFSListener()

    Disposer.register(project, () => closeReaderIfNeed(IndexCloseReason.PROJECT_CLOSED))
  }
  
  private def projectOutputRoots: Seq[String] = project.scala
}
