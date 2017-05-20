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

import scala.util.Try

import org.spongepowered.api.text.TextTemplate

import io.github.katrix.katlib.KatPlugin
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.persistant.CommentedConfigValue
import ninja.leaping.configurate.commented.CommentedConfigurationNode
import ninja.leaping.configurate.objectmapping.ObjectMappingException

class WelcomeConfigV1(cfgRoot: CommentedConfigurationNode, default: WelcomeConfig)(implicit plugin: KatPlugin) extends WelcomeConfig {

  override val version:         CommentedConfigValue[String]               = configValue(default.version)
  override val welcomeMessage:  CommentedConfigValue[Option[TextTemplate]] = configValue(default.welcomeMessage)
  override val welcomeCommands: CommentedConfigValue[Seq[String]]          = configValue(default.welcomeCommands)

  def configValue[A](existing: CommentedConfigValue[A])(implicit plugin: KatPlugin): CommentedConfigValue[A] =
    Try(Option(cfgRoot.getNode(existing.path: _*).getValue(existing.typeToken)).get)
      .map(found => existing.value_=(found)) //Doesn't want to work with CommentedConfigValue
      .recover {
        case e: ObjectMappingException =>
          LogHelper.error(s"Failed to deserialize value of ${existing.path.mkString(".")}, using the default instead", e)
          existing
        case _: NoSuchElementException =>
          LogHelper.warn(s"No value found for ${existing.path.mkString(".")}, using default instead")
          existing
      }
      .get
}
