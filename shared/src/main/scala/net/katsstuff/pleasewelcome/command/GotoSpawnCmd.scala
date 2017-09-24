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

package net.katsstuff.pleasewelcome.command

import java.util.Locale

import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.command.{CmdPlugin, LocalizedCommand}
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.pleasewelcome.lib.LibPerm
import net.katsstuff.pleasewelcome.{PWResource, PleaseWelcomePlugin}
import shapeless.Typeable

class GotoSpawnCmd(parent: CmdPlugin)(implicit plugin: PleaseWelcomePlugin) extends LocalizedCommand(Some(parent)) {
  val playerTypeable: Typeable[Player] = Typeable[Player]

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val executed = for {
      player    <- playerTypeable.cast(src).toRight(nonPlayerErrorLocalized)
      transform <- plugin.data.loginTransform.toRight(new CommandException(PWResource.getText("cmd.gotoSpawn.locationError")))
      text <- Either.cond(
        player.setLocationAndRotationSafely(transform.getLocation, transform.getRotation),
        t"$GREEN${PWResource.get("cmd.gotoSpawn.success")}",
        new CommandException(PWResource.getText("cmd.gotoSpawn.locationError"))
      )
    } yield text

    executed match {
      case Right(t) =>
        src.sendMessage(t)
        CommandResult.success()
      case Left(e) => throw e
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] = Some(PWResource.getText("cmd.gotoSpawn.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(this)
      .executor(this)
      .permission(LibPerm.GotoSpawn)
      .build()

  override def aliases: Seq[String] = Seq("gotoSpawn")
}
