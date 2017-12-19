package com.fpd.teamcity.slack.controllers

import com.fpd.teamcity.slack.ConfigManager.BuildSetting
import jetbrains.buildServer.serverSide.{Branch, BuildHistory, SFinishedBuild}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.TableDrivenPropertyChecks._

import scala.collection.JavaConverters._

class BuildSettingsTryTest extends FlatSpec with MockFactory with Matchers {
  "BuildSettingsTry.findPreviousBuild" should "work" in {
    val buildTypeId = "MyBuildTypeId"

    // Branches
    val branchMaster = stub[Branch]
    branchMaster.getDisplayName _ when() returns "master"

    val branchDefault = stub[Branch]
    branchDefault.getDisplayName _ when() returns "default"

    // Builds
    val buildWithoutBranch = stub[SFinishedBuild]
    buildWithoutBranch.getBranch _ when() returns null
    buildWithoutBranch.getBuildTypeId _ when() returns buildTypeId

    val buildDefault = stub[SFinishedBuild]
    buildDefault.getBranch _ when() returns branchDefault
    buildDefault.getBuildTypeId _ when() returns buildTypeId

    val buildMaster = stub[SFinishedBuild]
    buildMaster.getBranch _ when() returns branchMaster
    buildMaster.getBuildTypeId _ when() returns buildTypeId

    // Build histories
    val emptyBuildHistory = stub[BuildHistory]
    emptyBuildHistory.getEntries _ when * returns Seq[SFinishedBuild]().asJava

    val buildHistoryWithMatch = stub[BuildHistory]
    buildHistoryWithMatch.getEntries _ when * returns Seq(buildWithoutBranch).asJava

    val buildHistoryWithMatch2 = stub[BuildHistory]
    buildHistoryWithMatch2.getEntries _ when * returns Seq(buildDefault).asJava

    val buildHistoryWithoutMatch = stub[BuildHistory]
    buildHistoryWithoutMatch.getEntries _ when * returns Seq(buildMaster).asJava

    // settings
    val settingMatchAll = BuildSetting(buildTypeId, ".*", "", "")
    val settingMatchDefault = BuildSetting(buildTypeId, "default", "", "")

    // Assertion
    forAll(data) { (buildHistory: BuildHistory, buildSetting: BuildSetting, found: Option[SFinishedBuild]) ⇒
      BuildSettingsTry.findPreviousBuild(buildHistory, buildSetting) shouldEqual found
    }

    def data =
      Table(
        ("buildHistory", "buildSetting", "found"), // First tuple defines column names
        // Subsequent tuples define the data
        (emptyBuildHistory, settingMatchAll, None),
        (buildHistoryWithMatch, settingMatchAll, Some(buildWithoutBranch)),
        (buildHistoryWithoutMatch, settingMatchDefault, None),
        (buildHistoryWithMatch2, settingMatchDefault, Some(buildDefault))
      )
  }
}
