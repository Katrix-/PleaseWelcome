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

import java.util.UUID

import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.world.{Location, World}

import com.flowpowered.math.vector.Vector3d

import io.github.katrix.katlib.helper.Implicits._

case class PlayerPosition(x: Double, y: Double, z: Double, worldUuid: UUID, pitch: Double, yaw: Double, roll: Double) {
  def transform: Option[Transform[World]] = {
    Sponge.getServer.getWorld(worldUuid).toOption.map { world =>
      val location = new Location(world, x, y, z)
      val rotation = new Vector3d(pitch, yaw, roll)
      new Transform(location, rotation, Vector3d.ONE)
    }
  }
}