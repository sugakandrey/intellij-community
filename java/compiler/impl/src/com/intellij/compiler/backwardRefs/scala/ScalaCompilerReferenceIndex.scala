// Copyright 2000-2017 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.compiler.backwardRefs.scala

import java.io.File

import org.jetbrains.jps.backwardRefs.index.CompilerReferenceIndex

class ScalaCompilerReferenceIndex(buildDir: File, readOnly: Boolean)
    extends CompilerReferenceIndex[ClassfileData](ScalaCompilerIndexDescriptor, buildDir, readOnly)
