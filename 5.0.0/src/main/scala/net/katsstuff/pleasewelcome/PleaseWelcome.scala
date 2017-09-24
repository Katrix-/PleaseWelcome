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

import java.nio.file.Path
import java.util.UUID

import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.item.inventory.{Inventory, ItemStack}
import org.spongepowered.api.plugin.{Dependency, Plugin, PluginContainer}
import org.spongepowered.api.world.{Location, World}

import com.google.inject.Inject

import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.lib.LibKatLibPlugin
import io.github.katrix.katlib.serializer.TypeSerializerImpl
import io.github.katrix.katlib.serializer.TypeSerializerImpl._
import io.github.katrix.katlib.{ImplKatPlugin, KatLib}
import net.katsstuff.pleasewelcome.command.{GotoSpawnCmd, RemoveSpawnCmd, SendToSpawnCmd, SetInventoryCmd, SetSpawnCmd}
import net.katsstuff.pleasewelcome.handler.{WelcomeData, WelcomeHandler}
import net.katsstuff.pleasewelcome.lib.LibPlugin
import net.katsstuff.pleasewelcome.persistant.{StorageLoader, WelcomeConfig, WelcomeConfigLoader}
import ninja.leaping.configurate.objectmapping.serialize.{TypeSerializer, TypeSerializers}

object PleaseWelcome {

  final val Version         = s"1.1.0-${KatLib.CompiledAgainst}"
  final val ConstantVersion = "1.1.0-5.0.0"
  assert(Version == ConstantVersion)
}

@Plugin(
  id = LibPlugin.Id,
  name = LibPlugin.Name,
  description = LibPlugin.Description,
  authors = Array("Katrix"),
  version = PleaseWelcome.ConstantVersion,
  dependencies = Array(new Dependency(id = LibKatLibPlugin.Id, version = KatLib.ConstantVersion))
)
class PleaseWelcome @Inject()(
    logger: Logger,
    @ConfigDir(sharedRoot = false) cfgDir: Path,
    spongeContainer: PluginContainer
) extends ImplKatPlugin(logger, cfgDir, spongeContainer)
    with PleaseWelcomePlugin {

  implicit private val plugin: PleaseWelcome = this

  implicit val versionHelper = new VersionHelper {
    override def createFakeLocatedSource(source: CommandSource, location: Location[World]): CommandSource =
      new FakeLocatedSource(source, location)
    override def isInventoryEmpty(inventory: Inventory): Boolean = inventory.size() == 0
  }

  implicit private lazy val configLoader  = new WelcomeConfigLoader(configDir)
  implicit private lazy val storageLoader = new StorageLoader(configDir)
  private lazy val listener               = new WelcomeHandler(_config, _data)

  private var _config: WelcomeConfig = _
  override def config: WelcomeConfig = _config

  private var _data: WelcomeData = _
  override def data: WelcomeData = _data
  override def data_=(newData: WelcomeData): Unit = {
    _data = newData
    storageLoader.saveData(newData)
    listener.reload()
  }

  @Listener
  def init(event: GameInitializationEvent): Unit = {
    val serializers = TypeSerializers.getDefaultSerializers

    implicit val uuidSerializer = TypeSerializerImpl.fromTypeSerializer(serializers.get(typeToken[UUID]), classOf[UUID])
    implicit val stackSerializer =
      TypeSerializerImpl.fromTypeSerializer(serializers.get(typeToken[ItemStack]), classOf[ItemStack])
    serializers.registerType(typeToken[WelcomeData], implicitly[TypeSerializer[WelcomeData]])

    _config = configLoader.loadData
    _data = storageLoader.loadData

    Sponge.getEventManager.registerListeners(this, listener)

    addChildrenSpec(
      pluginCmd,
      new SetSpawnCmd(pluginCmd),
      new GotoSpawnCmd(pluginCmd),
      new RemoveSpawnCmd(pluginCmd),
      new SetInventoryCmd(pluginCmd),
      new SendToSpawnCmd(pluginCmd)
    )

    pluginCmd.registerHelp()
    Sponge.getCommandManager.register(this, pluginCmd.commandSpec, pluginCmd.aliases: _*)
  }

  @Listener
  def reload(event: GameReloadEvent): Unit = {
    configLoader.reload()
    _config = configLoader.loadData
    storageLoader.reload()
    _data = storageLoader.loadData
    listener.reload()
  }
}
