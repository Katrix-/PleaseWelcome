package net.katsstuff.pleasewelcome.command

import java.util.Locale

import org.spongepowered.api.command.args.{CommandContext, GenericArguments}
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.command.{CommandException, CommandResult, CommandSource}
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.format.TextColors.GREEN

import io.github.katrix.katlib.command.{CmdPlugin, LocalizedCommand}
import io.github.katrix.katlib.helper.Implicits._
import io.github.katrix.katlib.i18n.Localized
import io.github.katrix.katlib.lib.LibCommonTCommandKey
import net.katsstuff.pleasewelcome.lib.LibPerm
import net.katsstuff.pleasewelcome.{PWResource, PleaseWelcomePlugin}

class SendToSpawnCmd(parent: CmdPlugin)(implicit plugin: PleaseWelcomePlugin) extends LocalizedCommand(Some(parent)) {

  override def execute(src: CommandSource, args: CommandContext): CommandResult = Localized(src) { implicit locale =>
    val executed = for {
      player <- args.one(LibCommonTCommandKey.Player).toRight(invalidParameterErrorLocalized)
      transform <- plugin.data.loginTransform
        .toRight(new CommandException(PWResource.getText("command.error.spawnTransform")))
      text <- Either.cond(
        player.setLocationAndRotationSafely(transform.getLocation, transform.getRotation),
        t"$GREEN${PWResource.get("cmd.sendToSpawn.success", "player" -> player.getName)}",
        new CommandException(PWResource.getText("command.error.spawnTransform"))
      )
    } yield text

    executed match {
      case Right(t) =>
        src.sendMessage(t)
        CommandResult.success()
      case Left(e) => throw e
    }
  }

  override def localizedDescription(implicit locale: Locale): Option[Text] =
    Some(PWResource.getText("cmd.sendToSpawn.description"))

  override def commandSpec: CommandSpec =
    CommandSpec
      .builder()
      .description(this)
      .arguments(GenericArguments.player(LibCommonTCommandKey.Player))
      .executor(this)
      .permission(LibPerm.SendToSpawn)
      .build()

  override def aliases: Seq[String] = Seq("sendToSpawn")
}
