package patches.projects

import jetbrains.buildServer.configs.kotlin.v2019_2.*
import jetbrains.buildServer.configs.kotlin.v2019_2.Project
import jetbrains.buildServer.configs.kotlin.v2019_2.ui.*

/*
This patch script was generated by TeamCity on settings change in UI.
To apply the patch, change the root project
accordingly, and delete the patch script.
*/
changeProject(DslContext.projectId) {
    expectBuildTypesOrder(RelativeId("FormatBuildifier"), RelativeId("BuildBuildBazelBsp"), RelativeId("UnitTestsUnitTests"), RelativeId("E2eTestsE2eSampleRepoTest"), RelativeId("E2eTestsE2eLocalJdkTest"), RelativeId("E2eTestsE2eRemoteJdkTest"), RelativeId("E2eTestsE2eServerDownloadsBazeliskTest"), RelativeId("E2eTestsE2eKotlinProjectTest"), RelativeId("E2eTestsE2eAndroidProjectTest"), RelativeId("E2eTestsE2eAndroidKotlinProjectTest"), RelativeId("E2eTestsE2eEnabledRulesTest"), RelativeId("E2eTestsPluginRun"), RelativeId("Benchmark1001Targets"), RelativeId("BazelBspResults"))
    buildTypesOrderIds = arrayListOf(RelativeId("FormatBuildifier"), RelativeId("BuildBuildBazelBsp"), RelativeId("UnitTestsUnitTests"), RelativeId("E2eTestsE2eSampleRepoTest"), RelativeId("E2eTestsE2eLocalJdkTest"), RelativeId("E2eTestsE2eRemoteJdkTest"), RelativeId("E2eTestsE2eServerDownloadsBazeliskTest"), RelativeId("E2eTestsE2eKotlinProjectTest"), RelativeId("E2eTestsE2eAndroidProjectTest"), RelativeId("E2eTestsE2eAndroidKotlinProjectTest"), RelativeId("E2eTestsE2eEnabledRulesTest"), RelativeId("E2eTestsPluginRun"), RelativeId("Benchmark1001targets"), RelativeId("BazelBspResults"))
}
