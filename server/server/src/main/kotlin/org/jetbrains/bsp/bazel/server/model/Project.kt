package org.jetbrains.bsp.bazel.server.model

import org.jetbrains.bsp.protocol.BazelInfo
import java.net.URI

/** Project is the internal model of the project. Bazel/Aspect Model -> Project -> BSP Model  */
data class Project(
  val workspaceRoot: URI,
  val modules: List<Module>,
  val sourceToTarget: Map<URI, Label>,
  val libraries: Map<Label, Library>,
  val goLibraries: Map<Label, GoLibrary>,
  val invalidTargets: List<Label>,
  val nonModuleTargets: List<NonModuleTarget>, // targets that should be displayed in the project view but are neither modules nor libraries
  val bazelInfo: BazelInfo,
) {
  private val moduleMap: Map<Label, Module> = modules.associateBy(Module::label)

  fun findModule(label: Label): Module? = moduleMap[label]
}
