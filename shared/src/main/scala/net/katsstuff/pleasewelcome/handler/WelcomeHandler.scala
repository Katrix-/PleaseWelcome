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

import org.spongepowered.api.Sponge
import org.spongepowered.api.data.manipulator.mutable.entity.JoinData
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.network.ClientConnectionEvent

import io.github.katrix.katlib.helper.Implicits._
import net.katsstuff.pleasewelcome.VersionHelper
import net.katsstuff.pleasewelcome.persistant.{StorageLoader, WelcomeConfig, WelcomeConfigLoader}

class WelcomeHandler(
    startConfig: WelcomeConfig,
    startData: WelcomeData
)(implicit welcomeConfigLoader: WelcomeConfigLoader, storageLoader: StorageLoader, versionHelper: VersionHelper) {

  private var config      = startConfig
  private var welcomeData = startData

  def reload(): Unit = {
    config = welcomeConfigLoader.loadData
    welcomeData = storageLoader.loadData
  }

  @Listener
  def onFirstLogin(event: ClientConnectionEvent.Login): Unit = {
    val user = event.getTargetUser
    //Small buffer just in case
    val firstJoin = user.get(classOf[JoinData]).toOption.forall(j => j.lastPlayed().get().getEpochSecond - j.firstPlayed().get().getEpochSecond <= 3)

    if (firstJoin) {
      welcomeData.loginTransform.foreach(event.setToTransform)
    }
  }

  @Listener
  def inFirstJoin(event: ClientConnectionEvent.Join): Unit = {
    val player   = event.getTargetEntity
    val joinData = player.getJoinData
    //Small buffer just in case
    val firstJoin = joinData.lastPlayed().get().getEpochSecond - joinData.firstPlayed().get().getEpochSecond <= 3

    if (firstJoin) {
      welcomeData.onJoin(player, config)
      val locatedSource = versionHelper.createFakeLocatedSource(Sponge.getServer.getConsole, player.getLocation)
      config.welcomeCommands.value.foreach(Sponge.getCommandManager.process(locatedSource, _))
    }
  }
}
