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

package net.katsstuff.pleasewelcome

import java.util
import java.util.Optional

import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.service.context.Context
import org.spongepowered.api.service.permission.{Subject, SubjectCollection, SubjectData}
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.MessageChannel
import org.spongepowered.api.util.Tristate
import org.spongepowered.api.world.{Locatable, Location, World}

class FakeLocatedSource(commandSource: CommandSource, location: Location[World]) extends CommandSource with Locatable {

  override def getLocation: Location[World] = location

  override def getName: String = commandSource.getName

  override def getMessageChannel:                          MessageChannel = commandSource.getMessageChannel
  override def setMessageChannel(channel: MessageChannel): Unit           = commandSource.setMessageChannel(channel)
  override def sendMessage(message: Text):                 Unit           = commandSource.sendMessage(message)

  override def getCommandSource:        Optional[CommandSource] = commandSource.getCommandSource
  override def getContainingCollection: SubjectCollection       = commandSource.getContainingCollection
  override def getSubjectData:          SubjectData             = commandSource.getSubjectData
  override def getTransientSubjectData: SubjectData             = commandSource.getTransientSubjectData

  override def getPermissionValue(contexts: util.Set[Context], permission: String): Tristate =
    commandSource.getPermissionValue(contexts, permission)

  override def isChildOf(contexts: util.Set[Context], parent: Subject): Boolean =
    commandSource.isChildOf(contexts, parent)
  override def getParents(contexts: util.Set[Context]): util.List[Subject] = commandSource.getParents(contexts)
  override def getOption(contexts: util.Set[Context], key: String): Optional[String] =
    commandSource.getOption(contexts, key)

  override def getIdentifier:     String            = commandSource.getIdentifier
  override def getActiveContexts: util.Set[Context] = commandSource.getActiveContexts
}
