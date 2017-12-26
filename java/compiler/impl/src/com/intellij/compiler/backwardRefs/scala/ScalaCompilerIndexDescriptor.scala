// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.compiler.backwardRefs.scala

import java.io.{DataInput, DataOutput, File}
import java.util

import com.intellij.openapi.util.io.DataInputOutputUtilRt

import scala.collection.JavaConverters._
import com.intellij.util.indexing.{DataIndexer, IndexExtension, IndexId}
import com.intellij.util.io.{DataExternalizer, KeyDescriptor, VoidDataExternalizer}
import org.jetbrains.jps.backwardRefs.{LightRef, LightRefDescriptor}
import org.jetbrains.jps.backwardRefs.index.CompilerIndexDescriptor

object ScalaCompilerIndexDescriptor extends CompilerIndexDescriptor[ClassfileData] { self =>

  /** Increment every time indexing logic changes */
  val version = 1

  private val indexDir    = "sc-compiler-refs"
  private val versionFile = "version"

  val backwardUsages: IndexId[LightRef, Void]        = IndexId.create[LightRef, Void]("backward.usages")
  val backwardHierarchy: IndexId[LightRef, LightRef] = IndexId.create[LightRef, LightRef]("backward.hierarchy")

  override def getVersion: Int                      = version
  override def getIndicesDir(buildDir: File): File  = new File(buildDir, indexDir)
  override def getVersionFile(buildDir: File): File = new File(buildDir, versionFile)

  private def backwardUsagesExtension: IndexExtension[_, _, ClassfileData] =
    new IndexExtension[LightRef, Void, ClassfileData] {
      override def getName: IndexId[LightRef, Void]                       = backwardUsages
      override def getKeyDescriptor: KeyDescriptor[LightRef]              = LightRefDescriptor.INSTANCE
      override def getValueExternalizer: DataExternalizer[Void]           = VoidDataExternalizer.INSTANCE
      override def getVersion: Int                                        = self.getVersion
      override def getIndexer: DataIndexer[LightRef, Void, ClassfileData] = ???
    }

  private def backwardHierarchyExtension: IndexExtension[_, _, ClassfileData] =
    new IndexExtension[LightRef, LightRef, ClassfileData] {
      override def getName: IndexId[LightRef, LightRef]                            = backwardHierarchy
      override def getKeyDescriptor: KeyDescriptor[LightRef]                       = LightRefDescriptor.INSTANCE
      override def getValueExternalizer: DataExternalizer[LightRef]                = LightRefDescriptor.INSTANCE
      override def getVersion: Int                                                 = self.getVersion
      override def getIndexer: DataIndexer[LightRef, LightRef, ClassfileData] = ???
    }

  override def getIndices: util.Collection[IndexExtension[_, _, ClassfileData]] =
    Seq(
      backwardUsagesExtension,
      backwardHierarchyExtension
    ).asJava
}
