// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.compiler.backwardRefs.scala

import java.io.{File, IOException}
import java.util

import com.intellij.compiler.backwardRefs.{CompilerHierarchySearchType, CompilerReferenceReader, SearchId}
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.vfs.{VfsUtil, VirtualFile, VirtualFileWithId}
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.containers.Queue
import com.intellij.util.indexing.StorageException
import gnu.trove.{THashSet, TIntHashSet}
import org.jetbrains.jps.backwardRefs.LightRef

import scala.annotation.tailrec

class ScalaCompilerReferenceReader(
  buildDir: File
) extends CompilerReferenceReader[ScalaCompilerReferenceIndex](
      buildDir,
      new ScalaCompilerReferenceIndex(buildDir, true)
    ) {

  override def findReferentFileIds(ref: LightRef, checkBaseClassAmbiguity: Boolean): TIntHashSet =
    rethrowStorageExceptionIn {
      val hierarchy: Array[LightRef.NamedLightRef] = ref match {
        case classRef: LightRef.LightClassHierarchyElementDef => Array(classRef)
        case member: LightRef.LightMember =>
          getHierarchy(member.getOwner, checkBaseClassAmbiguity, includeAnonymous = true, -1)
            .map(identity) // scala arrays are invariant
        case _ => throw new IllegalArgumentException("Should never happen.")
      }

      val result = new TIntHashSet()

      hierarchy.foreach { owner =>
        val overridden = ref.`override`(owner.getName)
        myIndex.get(ScalaCompilerIndexDescriptor.backwardUsages).getData(overridden).forEach {
          case (id: Int, _) =>
            findFileByEnumeratorId(id).foreach(f => result.add(f.asInstanceOf[VirtualFileWithId].getId))
            true
        }
      }

      result
    }

  private def findFileByEnumeratorId(id: Int): Option[VirtualFile] = {
    val path = myIndex.getFilePathEnumerator.valueOf(id)
    val file = new File(path)
    
    try Option(VfsUtil.findFileByIoFile(file, false))
    catch { case e: IOException => throw new RuntimeException(e) }
  }

  override def getDirectInheritors(
    searchElement: LightRef,
    searchScope: GlobalSearchScope,
    dirtyScope: GlobalSearchScope,
    fileType: FileType,
    searchType: CompilerHierarchySearchType
  ): util.Map[VirtualFile, Array[SearchId]] = null

  override def getAnonymousCount(classDef: LightRef.LightClassHierarchyElementDef, checkDefinitions: Boolean): Integer =
    0
  override def getOccurrenceCount(element: LightRef): Int = 0

  override def getHierarchy(
    hierarchyElement: LightRef.LightClassHierarchyElementDef,
    checkBaseClassAmbiguity: Boolean,
    includeAnonymous: Boolean,
    interruptNumber: Int
  ): Array[LightRef.LightClassHierarchyElementDef] = rethrowStorageExceptionIn {
    val res   = new THashSet[LightRef.LightClassHierarchyElementDef]()
    val queue = new Queue[LightRef.LightClassHierarchyElementDef](10)

    @tailrec
    def drain(q: Queue[LightRef.LightClassHierarchyElementDef]): Unit = if (!queue.isEmpty) {
      if (interruptNumber == -1 || res.size() <= interruptNumber) {
        val currentClass = q.pullFirst()
        if (res.add(currentClass)) {
          if (res.size() % 100 == 0) {
            ProgressManager.checkCanceled()
          }
          myIndex.get(ScalaCompilerIndexDescriptor.backwardHierarchy).getData(currentClass).forEach {
            case (_, children: Seq[LightRef]) =>
              children.collect {
                case aClass: LightRef.LightClassHierarchyElementDef            => queue.addLast(aClass)
                case anon: LightRef.LightAnonymousClassDef if includeAnonymous => queue.addLast(anon)
              }
              true
          }
        }
        drain(q)
      }
    }

    queue.addLast(hierarchyElement)
    drain(queue)
    res.toArray(LightRef.LightClassHierarchyElementDef.EMPTY_ARRAY)
  }

  private def rethrowStorageExceptionIn[T](body: => T): T =
    try body
    catch { case e: StorageException => throw new RuntimeException(e) }
}
