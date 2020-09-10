package builds

import builds.default.DefaultBuild
import builds.default.DefaultVisualRegression
import builds.oss.OssBuild
import builds.oss.OssVisualRegression
import dependsOn
import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs

object BaselineCi : BuildType({
  id("Baseline_CI")
  name = "Baseline CI"
  type = Type.COMPOSITE
  paused = false

  triggers {
    vcs {
      perCheckinTriggering = true
    }
  }

  dependsOn(
    OssBuild,
    DefaultBuild,
    OssVisualRegression,
    DefaultVisualRegression
  )
})
