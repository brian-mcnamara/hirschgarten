package org.jetbrains.bazel.fastbuild

import com.intellij.execution.ExecutionListener
import com.intellij.execution.process.ProcessHandler
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.openapi.project.Project
import org.jetbrains.bazel.jvm.run.BazelExecutionArgumentProvider
import org.jetbrains.bazel.run.config.BazelRunConfiguration

class FastBuildArgumentProvider: BazelExecutionArgumentProvider {

  override fun isApplicable(bazelRunConfiguration: BazelRunConfiguration) = true

  override fun getAdditionalArguments(bazelRunConfiguration: BazelRunConfiguration): List<String> {
    val tempDir = JvmFastBuildClasspathProvider.getInstance().getFastBuildOutputPath(bazelRunConfiguration.targets.single())
    return listOf("--wrapper_script_flag=--main_advice_classpath=${tempDir}")
  }

  private class FastBuildExecutionListener(val project: Project): ExecutionListener {
    override fun processStarting(
      executorId: String,
      env: ExecutionEnvironment,
      handler: ProcessHandler
    ) {
      val configuration = env.runnerAndConfigurationSettings?.configuration
      if (configuration is BazelRunConfiguration) {
        JvmFastBuildClasspathProvider.getInstance().createTempDir(configuration.targets)
      }
    }

    override fun processTerminated(
      executorId: String,
      env: ExecutionEnvironment,
      handler: ProcessHandler,
      exitCode: Int
    ) {
      val configuration = env.runnerAndConfigurationSettings?.configuration
      if (configuration is BazelRunConfiguration) {
        JvmFastBuildClasspathProvider.getInstance().deleteDir(configuration.targets)
      }
    }
  }
}
