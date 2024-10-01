package org.jetbrains.bsp.bazel.bazelrunner.utils

import org.jetbrains.bsp.protocol.BazelInfo
import org.jetbrains.bsp.protocol.BazelRelease
import java.nio.file.Path

data class BasicBazelInfo(
  override val execRoot: String,
  override val outputBase: Path,
  override val workspaceRoot: Path,
  override val release: BazelRelease,
  override val isBzlModEnabled: Boolean,
  override val javaHome: Path,
) : BazelInfo

class LazyBazelInfo(bazelInfoSupplier: () -> BazelInfo) : BazelInfo {
  private val bazelInfo: BazelInfo by lazy { bazelInfoSupplier() }

  override val execRoot: String
    get() = bazelInfo.execRoot

  override val outputBase: Path
    get() = bazelInfo.outputBase

  override val workspaceRoot: Path
    get() = bazelInfo.workspaceRoot

  override val release: BazelRelease
    get() = bazelInfo.release

  override val isBzlModEnabled: Boolean
    get() = bazelInfo.isBzlModEnabled

  override val javaHome: Path
    get() = bazelInfo.javaHome
}
