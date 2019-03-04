/* Copyright 2018 UniCredit S.p.A.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.unicredit.sbt

import java.net.InetSocketAddress

import com.typesafe.sbt.packager.universal.UniversalPlugin.autoImport._
import com.typesafe.sbt.web.Import.Assets
import play.sbt.PlayImport.PlayKeys._
import play.sbt.{PlayWeb, PlayRunHook}
import sbt.Keys._
import sbt._

import scala.sys.process.Process

object SbtPlayNpmPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements
  override def requires: Plugins = PlayWeb

  object autoImport {
    lazy val npmDependenciesInstall = settingKey[String]("Command to install dependencies")
    lazy val npmTest = settingKey[String]("Command to run tests")
    lazy val npmServe = settingKey[String]("Command to start the application")
    lazy val npmBuild = settingKey[String]("Command to build the application")
    lazy val npmEnvOption = settingKey[Option[String]]("Environment option to add to the command")
    lazy val npmSrcDir = settingKey[String]("Folder name of the application source files")
    lazy val npmModulesDir = settingKey[String]("Folder name of the dependency modules")
    lazy val npmBuildDir = settingKey[String]("Folder name of the generated build files")
    lazy val npmTestTask = taskKey[Unit]("Task to run tests")
    lazy val npmBuildTask = taskKey[Unit]("Task to build the application")
    lazy val npmCleanTask = taskKey[Unit]("Task to clean the build folder")
  }

  import autoImport._

  override lazy val projectSettings: Seq[Setting[_]]  = super.projectSettings ++ Seq(
    npmDependenciesInstall := "npm install",
    npmTest := "npm run test",
    npmServe := "npm run start",
    npmBuild := "npm run build",
    npmEnvOption := Some("CI=true"),
    npmSrcDir := "web",
    npmModulesDir := npmSrcDir.value + "/node_modules",
    npmBuildDir := npmSrcDir.value + "/build",
    npmTestTask := {
      val dir = baseDirectory.value / npmSrcDir.value
      if (!((baseDirectory.value / npmModulesDir.value).exists() || runProcess(npmDependenciesInstall.value, npmEnvOption.value, dir)) ||
        !runProcess(npmTest.value, npmEnvOption.value, dir))
        throw new Exception("Tests failed!")
    },
    npmBuildTask := {
      val dir = baseDirectory.value / npmSrcDir.value
      if (!((baseDirectory.value / npmModulesDir.value).exists() || runProcess(npmDependenciesInstall.value, npmEnvOption.value, dir)) ||
        !runProcess(npmBuild.value, npmEnvOption.value, dir))
        throw new Exception("Build failed!")
    },
    npmCleanTask := { IO.delete(baseDirectory.value / npmBuildDir.value) },
    dist := (dist dependsOn npmBuildTask).value,
    stage := (stage dependsOn npmBuildTask).value,
    test := ((test in Test) dependsOn npmTestTask).value,
    clean := (clean dependsOn npmCleanTask).value,
    unmanagedResourceDirectories in Assets += baseDirectory.value / npmBuildDir.value,
    playRunHooks += npmRunHook(npmServe.value, npmDependenciesInstall.value, baseDirectory.value / npmSrcDir.value)
  )

  private val isWindows = System.getProperty("os.name").toLowerCase().contains("win")
  private def command(script: String, option: Option[String] = None): String = {
    val cmd = if (option.isEmpty) script
      else if (isWindows) s"set ${option.get}&&$script"
      else s"env ${option.get} $script"

    if (isWindows) s"cmd /c $cmd" else cmd
  }

  private def runProcess(script: String, option: Option[String], dir: File): Boolean =
    Process(command(script, option), dir).! == 0

  // Run serve task when Play runs in dev mode, that is, when using 'sbt run'
  // https://www.playframework.com/documentation/2.6.x/SBTCookbook
  def npmRunHook(serve: String, install: String, dir: File): PlayRunHook = {
    object NpmRunHook extends PlayRunHook {

      var process: Option[Process] = None

      /**
        * Executed before play run start.
        * Run npm install if node modules are not installed.
        */
      override def beforeStarted(): Unit = Process(command(install), dir).!

      /**
        * Executed after play run start.
        * Run npm start
        */
      override def afterStarted(): Unit = {
        process = Option(Process(command(serve), dir).run)
      }

      /**
        * Executed after play run stop.
        * Cleanup frontend execution processes.
        */
      override def afterStopped(): Unit = {
        process.foreach(_.destroy())
        process = None
      }

    }

    NpmRunHook
  }
}