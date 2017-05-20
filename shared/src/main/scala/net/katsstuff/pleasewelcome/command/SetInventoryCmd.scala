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

import org.spongepowered.api.command.{CommandResult, CommandSource}
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.player.Player

import io.github.katrix.katlib.command.{CmdPlugin, CommandBase}
import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.pleasewelcome.{PleaseWelcomePlugin, VersionHelper}
import net.katsstuff.pleasewelcome.lib.LibPerm
import shapeless.Typeable
import org.spongepowered.api.text.format.TextColors._
import scala.collection.JavaConverters._

import org.spongepowered.api.item.inventory.Slot
import org.spongepowered.api.item.inventory.property.SlotIndex

import io.github.katrix.katlib.helper.LogHelper

class SetInventoryCmd(parent: CmdPlugin)(implicit plugin: PleaseWelcomePlugin, versionHelper: VersionHelper) extends CommandBase(Some(parent)) {
  val playerTypeable: Typeable[Player] = Typeable[Player]

  override def execute(src: CommandSource, args: CommandContext): CommandResult = {
    val newData = for {
      player <- playerTypeable.cast(src).toRight(nonPlayerError)
    } yield {
      val inventory = player.getInventory.slots[Slot].asScala.toSeq.flatMap { s =>
        s.peek().toOption.map(stack => s.getProperties(classOf[SlotIndex]).asScala.headOption.map(_.getValue.intValue()) -> stack)
      }

      val allWithIndex = if (inventory.forall(_._1.isDefined)) {
        inventory.map(t => t._1.get -> t._2)
      } else if (!inventory.exists(_._1.isDefined)) {
        LogHelper.debug("No slot index found. Will fall back to zipWithIndex index")
        player.getInventory.slots[Slot].asScala.toSeq.zipWithIndex.collect {
          case (slot, i) if !versionHelper.isInventoryEmpty(slot) => i -> slot.peek().get()
        }
      } else {
        LogHelper.warn("Found mixed slot index and no slot index. Will try to merge as best as possible")
        val usedIndicies = inventory.flatMap(_._1)
        val indicies     = 0 to inventory.size filterNot usedIndicies.contains

        val withIndex = inventory.collect {
          case (Some(i), stack) => i -> stack
        }

        val withoutIndex = inventory.collect {
          case (None, stack) => stack
        }

        indicies.zip(withoutIndex) ++ withIndex
      }

      plugin.data.copy(items = allWithIndex.toMap)
    }

    newData match {
      case Right(data) =>
        plugin.data = data
        src.sendMessage(t"${GREEN}Set new welcome inventory")
        CommandResult.success()
      case Left(e) => throw e
    }
  }

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(t"Set the welcome spawn to where you are standing")
      .executor(this)
      .permission(LibPerm.SetInventory)
      .build()

  override def aliases: Seq[String] = Seq("setInventory")
}
