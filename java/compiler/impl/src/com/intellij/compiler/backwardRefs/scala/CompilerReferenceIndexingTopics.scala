// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.compiler.backwardRefs.scala

import com.intellij.util.messages.Topic

object CompilerReferenceIndexingTopics {
  val indexingStatus: Topic[IndexingStatusListener] =
    Topic.create[IndexingStatusListener]("compiler reference index build status", classOf[IndexingStatusListener])
}
