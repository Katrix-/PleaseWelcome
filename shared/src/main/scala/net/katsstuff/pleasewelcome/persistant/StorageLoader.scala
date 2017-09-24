/*
 * This file is part of PleaseWelcome, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2017 Katrix
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package net.katsstuff.pleasewelcome.persistant

import java.nio.file.Path

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.ConfigurateBase
import net.katsstuff.pleasewelcome.handler.WelcomeData
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.gson.GsonConfigurationLoader

class StorageLoader(dir: Path)(implicit plugin: KatPlugin)
    extends ConfigurateBase[WelcomeData, ConfigurationNode, GsonConfigurationLoader](
      dir,
      "storage.json",
      path => GsonConfigurationLoader.builder().setPath(path).build()
    ) {

  private val welcomeTypeToken = typeToken[WelcomeData]

  override def loadData: WelcomeData = {
    versionNode.getString("1") match {
      case "1" =>
        Option(welcomeNode.getValue(welcomeTypeToken)) match {
          case Some(data) =>
            saveData(data)
            data
          case None =>
            LogHelper.error("Could not load welcome data from storage")
            WelcomeData(None, Map.empty)
        }
      case _ =>
        if(versionNode.getString != null) {
          LogHelper.error("Invalid storage version. Could not load welcome data from storage")
        }
        else {
          saveData(WelcomeData(None, Map.empty))
        }
        WelcomeData(None, Map.empty)
    }
  }

  override def saveData(data: WelcomeData): Unit = {
    versionNode.setValue("1")
    welcomeNode.setValue(welcomeTypeToken, data)

    saveFile()
  }

  private def welcomeNode: ConfigurationNode = cfgRoot.getNode("welcome")
  private def versionNode: ConfigurationNode = cfgRoot.getNode("version")

  def reload(): Unit = cfgRoot = cfgLoader.load()
}
