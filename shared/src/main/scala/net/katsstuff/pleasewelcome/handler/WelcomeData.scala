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

package net.katsstuff.pleasewelcome.handler

import scala.collection.JavaConverters._

import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.`type`.OrderedInventory
import org.spongepowered.api.item.inventory.property.SlotIndex
import org.spongepowered.api.text.channel.MessageChannel
import org.spongepowered.api.world.World

import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.pleasewelcome.persistant.WelcomeConfig

case class WelcomeData(spawnPos: Option[PlayerPosition], items: Map[Int, ItemStack]) {

  def loginTransform: Option[Transform[World]] = spawnPos.flatMap(_.transform)

  def onJoin(player: Player, config: WelcomeConfig): Unit = {
    player.getInventory match {
      case inventory: OrderedInventory =>
        for ((pos, stack) <- items) {
          inventory.set(new SlotIndex(pos), stack)
        }
      case inventory =>
        items.values.foreach { stack =>
          inventory.offer(stack)
        }
    }

    config.welcomeMessage.value
      .map(_.apply(Map(config.NewPlayer -> player.getName.text).asJava).build())
      .foreach(MessageChannel.TO_ALL.send)
  }
}
