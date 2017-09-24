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

import scala.collection.JavaConverters._

import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.Slot
import org.spongepowered.api.item.inventory.property.SlotIndex
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors._

import io.github.katrix.katlib.command.{CmdPlugin, LocalizedCommand}
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.helper.LogHelper
import io.github.katrix.katlib.i18n.Localized
import net.katsstuff.pleasewelcome.lib.LibPerm
import net.katsstuff.pleasewelcome.{PWResource, PleaseWelcomePlugin, VersionHelper}
import shapeless.Typeable

class SetInventoryCmd(parent: CmdPlugin)(implicit plugin: PleaseWelcomePlugin, versionHelper: VersionHelper)
    extends LocalizedCommand(Some(parent)) {
  val playerTypeable: Typeable[Player] = Typeable[Player]

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val newData = for {
      player <- playerTypeable.cast(src).toRight(nonPlayerErrorLocalized)
    } yield player

    newData match {
      case Right(player) =>
        val slots = player.getInventory.slots[Slot].asScala
        val indexToSlot = slots.flatMap { slot =>
          val content = slot.peek.toOption
          val index   = slot.getProperties(classOf[SlotIndex]).asScala.headOption.map(_.getValue.intValue)
          content.map(stack => index -> stack)
        }.toMap

        val itemsMap = if (indexToSlot.forall(_._1.isDefined)) {
          indexToSlot.map(t => t._1.get -> t._2)
        } else if (!indexToSlot.exists(_._1.isDefined)) {
          LogHelper.debug("No slot index found. Will fall back to zipWithIndex index")
          slots.zipWithIndex.collect {
            case (slot, i) if !versionHelper.isInventoryEmpty(slot) => i -> slot.peek.get
          }.toMap
        } else {
          LogHelper.warn("Found mixed slot index and no slot index. Will try to merge as best as possible")
          val usedIndices = indexToSlot.flatMap(_._1).toSeq
          val newIndices  = (0 to indexToSlot.size).filterNot(usedIndices.contains)

          val withIndex = indexToSlot.collect {
            case (Some(i), stack) => i -> stack
          }

          val withoutIndex = indexToSlot.collect {
            case (None, stack) => stack
          }

          newIndices.zip(withoutIndex).toMap ++ withIndex
        }

        plugin.data = plugin.data.copy(items = itemsMap)
        src.sendMessage(t"$GREEN${PWResource.get("cmd.setInv.success")}")
        CommandResult.success()
      case Left(e) => throw e
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(PWResource.getText("cmd.setInv.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(this)
      .executor(this)
      .permission(LibPerm.SetInventory)
      .build()

  override def aliases: Seq[String] = Seq("setInventory", "setInv")
}
