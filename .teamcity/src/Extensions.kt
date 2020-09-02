import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.buildFeatures.notifications
import projects.kibanaConfiguration

fun BuildFeatures.junit(dirs: String = "target/**/TEST-*.xml") {
  feature {
    type = "xml-report-plugin"
    param("xmlReportParsing.reportType", "junit")
    param("xmlReportParsing.reportDirs", dirs)
  }
}

fun ProjectFeatures.kibanaAgent(init: ProjectFeature.() -> Unit) {
  feature {
    type = "CloudImage"
    param("network", kibanaConfiguration.agentNetwork)
    param("subnet", kibanaConfiguration.agentSubnet)
    param("growingId", "true")
    param("agent_pool_id", "-2")
    param("sourceProject", "elastic-kibana-184716")
//      param("source-id", "elastic-kibana-ci-ubuntu-1804-lts-")
    param("preemptible", "false")
//      param("sourceImageFamily", "elastic-kibana-ci-ubuntu-1804-lts")
//      param("sourceImageFamily", "kibana-teamcity-dev-agents")
    param("sourceImageFamily", "kibana-ci-elastic-dev")
    param("zone", "us-central1-a")
    param("profileId", "kibana-brianseeders")
    param("diskType", "pd-ssd")
    param("machineCustom", "false")
    param("maxInstances", "20")
    param("imageType", "ImageFamily")
    param("diskSizeGb", "")
    init()
  }
}

fun ProjectFeatures.kibanaAgent(size: String, init: ProjectFeature.() -> Unit = {}) {
  kibanaAgent {
    id = "KIBANA_BRIANSEEDERS_STANDARD_$size"
    param("source-id", "kibana-standard-$size-")
    param("machineType", "n2-standard-$size")
    init()
  }
}

fun BuildType.kibanaAgent(size: String) {
  requirements {
    startsWith("teamcity.agent.name", "kibana-standard-$size-", "RQ_AGENT_NAME")
  }
}

fun BuildType.kibanaAgent(size: Int) {
  kibanaAgent(size.toString())
}

fun BuildType.addTestArtifacts() {
  this.artifactRules += "\n" + """
    target/kibana-*
    target/test-metrics/*
    target/kibana-security-solution/**/*.png
    target/junit/**/*
    target/test-suites-ci-plan.json
    test/**/screenshots/session/*.png
    test/**/screenshots/failure/*.png
    test/**/screenshots/diff/*.png
    test/functional/failure_debug/html/*.html
    x-pack/test/**/screenshots/session/*.png
    x-pack/test/**/screenshots/failure/*.png
    x-pack/test/**/screenshots/diff/*.png
    x-pack/test/functional/failure_debug/html/*.html
    x-pack/test/functional/apps/reporting/reports/session/*.pdf
  """.trimIndent()
}

fun BuildType.addSlackNotifications(to: String = "#kibana-teamcity-testing") {
  features {
    notifications {
      notifierSettings = slackNotifier {
        connection = "KIBANA_SLACK"
        sendTo = to
        messageFormat = verboseMessageFormat {
          addBranch = true
          addChanges = true
          addStatusText = true
          maximumNumberOfChanges = 5
        }
      }
      buildFailedToStart = true
      buildFailed = true
      buildFinishedSuccessfully = true
      firstBuildErrorOccurs = true
      buildProbablyHanging = true
    }
  }
}

fun BuildType.dependsOn(buildType: BuildType, init: SnapshotDependency.() -> Unit = {}) {
  dependencies {
    snapshot(buildType) {
      reuseBuilds = ReuseBuilds.SUCCESSFUL
      onDependencyCancel = FailureAction.CANCEL
      onDependencyFailure = FailureAction.CANCEL
      synchronizeRevisions = true
      init()
    }
  }
}

fun BuildType.dependsOn(vararg buildTypes: BuildType, init: SnapshotDependency.() -> Unit = {}) {
  buildTypes.forEach { dependsOn(it, init) }
}
