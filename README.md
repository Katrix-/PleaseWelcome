# PleaseWelcome

PleaseWelcome is a Sponge plugin to decide what happens when a new player joins the server.

## Commands
* `/pleasewelcome setspawn` Sets the spawn to teleport people to when they join for the first time.
* `/pleasewelcome gotospawn` Teleport to the spawn location
* `/pleasewelcome removespawn` Removed the spawn location. New players will spawn at the default spawn.
* `/pleasewelcome setinventory` Sets the inventory to give players when they join.

## Permissions
* `pleasewelcome.mod.setspawn` Allows a player to use `/pleasewelcome setspawn`
* `pleasewelcome.mod.gotospawn` Allows a player to use `/pleasewelcome gotospawn`
* `pleasewelcome.mod.removespawn` Allows a player to use `/pleasewelcome removespawn`
* `pleasewelcome.mod.setinventory` Allows a player to use `/pleasewelcome setinventory`

## Configuration
When PleaseWelcome starts for the first time, it will generate a config where you can set misc settings.
* `welcome.commands` A List of all the commands that will be executed when the player joins. You can freely use target selectors in these commands. For example, use `@p` to get the player that joined.
* `welcome.message` The message to show when a player joins the server for the first time. This is an TextTemplate.