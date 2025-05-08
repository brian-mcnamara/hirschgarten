package org.jetbrains.bazel.jvm.run

import com.intellij.openapi.extensions.ExtensionPointName
import org.jetbrains.bazel.run.config.BazelRunConfiguration

interface BazelExecutionArgumentProvider {

  fun isApplicable(bazelRunConfiguration: BazelRunConfiguration): Boolean
  fun getAdditionalArguments(bazelRunConfiguration: BazelRunConfiguration): List<String>

  companion object {
    val ep = ExtensionPointName.create<BazelExecutionArgumentProvider>("org.jetbrains.bazel.executionArgumentProvider")
  }
}
