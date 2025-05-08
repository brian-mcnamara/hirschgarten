package org.jetbrains.bazel.fastbuild

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.Service
import com.intellij.util.io.delete
import org.jetbrains.bazel.label.Label
import java.nio.file.Files
import java.nio.file.Path

@Service
class JvmFastBuildClasspathProvider {
  private val pathsCache = HashMap<Label, Path>()

  internal fun createTempDir(labels: List<Label>) {
    Files.createTempDirectory("bspFastBuild").also { tmpDir ->
      tmpDir.toFile().deleteOnExit()
      labels.forEach { pathsCache.put(it, tmpDir) }
    }
  }

  internal fun deleteDir(labels: List<Label>) {
    labels.mapNotNull { pathsCache.remove(it) }
      .forEach { it.delete(true) }
  }

  fun getFastBuildOutputPath(label: Label): Path {
    return pathsCache.get(label) ?: TODO()
  }

  companion object {
    fun getInstance(): JvmFastBuildClasspathProvider = ApplicationManager.getApplication().getService(JvmFastBuildClasspathProvider::class.java)
  }
}
