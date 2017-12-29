// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.compiler.backwardRefs.scala

import java.util

import org.jetbrains.jps.backwardRefs.LightRef

final case class ClassfileData(aClass: LightRef, superClasses: Seq[LightRef], refs: Seq[LightRef]) {
  private[this] val VoidLiteral: Void = null
  
  def indexableUsages: util.Map[LightRef, Void] = {
    val usages = new util.HashMap[LightRef, Void](refs.size)
    refs.foreach(usages.put(_, VoidLiteral))
    usages
  }
  
  def indexableHierarchy: util.Map[LightRef, LightRef] = {
    val backHierarchy = new util.HashMap[LightRef, LightRef]()
    superClasses.foreach(backHierarchy.put(_, aClass))
    backHierarchy
  }
}
