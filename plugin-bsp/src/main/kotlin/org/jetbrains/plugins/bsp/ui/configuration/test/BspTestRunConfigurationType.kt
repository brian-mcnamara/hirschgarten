package org.jetbrains.plugins.bsp.ui.configuration.test

import com.intellij.execution.DefaultExecutionResult
import com.intellij.execution.Executor
import com.intellij.execution.configurations.ConfigurationFactory
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.RunConfigurationBase
import com.intellij.execution.configurations.RunProfileState
import com.intellij.execution.runners.ExecutionEnvironment
import com.intellij.execution.testframework.sm.runner.SMTRunnerConsoleProperties
import com.intellij.openapi.options.SettingsEditor
import com.intellij.openapi.project.Project
import org.jetbrains.magicmetamodel.impl.workspacemodel.toBsp4JTargetIdentifier
import org.jetbrains.plugins.bsp.assets.BuildToolAssetsExtension
import org.jetbrains.plugins.bsp.config.buildToolId
import org.jetbrains.plugins.bsp.flow.open.withBuildToolIdOrDefault
import org.jetbrains.plugins.bsp.server.tasks.TestTargetTask
import org.jetbrains.plugins.bsp.ui.configuration.BspProcessHandler
import org.jetbrains.plugins.bsp.ui.console.BspConsoleService
import org.jetbrains.plugins.bsp.ui.widgets.tool.window.actions.targetIdTOREMOVE
import javax.swing.Icon

public class BspTestRunConfigurationType(project: Project) : ConfigurationType {
  private val assetsExtension = BuildToolAssetsExtension.ep.withBuildToolIdOrDefault(project.buildToolId)

  override fun getDisplayName(): String = "${assetsExtension.presentableName} TEST"

  override fun getConfigurationTypeDescription(): String = "${assetsExtension.presentableName} TEST"

  override fun getIcon(): Icon = assetsExtension.icon

  override fun getId(): String = ID

  override fun getConfigurationFactories(): Array<ConfigurationFactory> =
    arrayOf(BspTestRunFactory(this))

  public companion object {
    public const val ID: String = "BspTestRunConfiguration"
  }
}

public class BspTestRunFactory(t: ConfigurationType) : ConfigurationFactory(t) {
  override fun createTemplateConfiguration(project: Project): RunConfiguration {
    val assetsExtension = BuildToolAssetsExtension.ep.withBuildToolIdOrDefault(project.buildToolId)
    return BspTestRunConfiguration(project, this, "${assetsExtension.presentableName} TEST")
  }

  override fun getId(): String =
    BspTestRunConfigurationType.ID
}

public class BspTestRunConfiguration(project: Project, configurationFactory: ConfigurationFactory, name: String) :
  RunConfigurationBase<String>(project, configurationFactory, name) {
  override fun getState(executor: Executor, environment: ExecutionEnvironment): RunProfileState {
    return RunProfileState { executor2, _ ->

      val bspTestConsole = BspConsoleService.getInstance(project).bspTestConsole

      val processHandler = BspProcessHandler()
      val testConsole = BspTestConsolePrinter(processHandler, SMTRunnerConsoleProperties(this, "BSP", executor2))
      environment.getUserData(targetIdTOREMOVE)?.let {
        bspTestConsole.registerPrinter(testConsole)
        processHandler.execute {
          try {
            // TODO error handling?
            TestTargetTask(project).connectAndExecute(it.toBsp4JTargetIdentifier())
          } finally {
            testConsole.endTesting()
            bspTestConsole.deregisterPrinter(testConsole)
          }
        }
      } ?: processHandler.shutdown()
      DefaultExecutionResult(testConsole.console, processHandler)
    }
  }

  override fun getConfigurationEditor(): SettingsEditor<out RunConfiguration> {
    // TODO https://youtrack.jetbrains.com/issue/BAZEL-628
    TODO("Not yet implemented")
  }
}
