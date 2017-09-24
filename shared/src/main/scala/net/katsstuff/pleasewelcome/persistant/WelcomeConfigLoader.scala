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

import scala.util.Try

import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.text.TextTemplate
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.{CommentedConfigValue, ConfigLoader, ConfigValue}

class WelcomeConfigLoader(dir: Path)(implicit plugin: KatPlugin) extends ConfigLoader[WelcomeConfig](dir, identity) {
  override def loadData: WelcomeConfig = {
    cfgRoot.getNode("version").getString("1") match {
      case "1" =>
        val loaded = new WelcomeConfigV1(cfgRoot, default)
        saveData(loaded)
        loaded
      case _ =>
        LogHelper.error("Invalid config version. Using default")
        default
    }
  }

  val default: WelcomeConfig = new WelcomeConfig {
    //Before 1.11 getId doesn't return something that can be used in a command
    def fireworkRocketName: String = {
      val version = Sponge.getPlatform.getMinecraftVersion.getName.split('.')
      if (version.length >= 2) {
        Try(version(1).toInt)
          .collect {
            case i if i < 11 => "FireworksRocketEntity"
          }
          .getOrElse(EntityTypes.FIREWORK.getId)
      } else EntityTypes.FIREWORK.getId
    }

    override val welcomeMessage: CommentedConfigValue[Option[TextTemplate]] = ConfigValue(
      Some(tt"$GOLD$NewPlayer just joined the server for the first time"),
      "The message shows when a player joins the server for the first time",
      Seq("welcome", "message")
    )

    override val welcomeCommands: CommentedConfigValue[Seq[String]] = {
      ConfigValue(
        Seq(
          s"execute @p ~ ~ ~ summon $fireworkRocketName ~ ~ ~ {LifeTime:20,FireworksItem:{id:fireworks,Count:1,tag:{Fireworks:{Explosions:[{Type:1,Flicker:1,Trail:1,Colors:[16776993],FadeColors:[16749107]}]}}}}"
        ),
        "The commands to execute when a new player joins the server",
        Seq("welcome", "commands")
      )
    }

    override val version: CommentedConfigValue[String] = ConfigValue("1", "Please don't change this", Seq("version"))
  }

  def reload(): Unit = cfgRoot = cfgLoader.load()
}
