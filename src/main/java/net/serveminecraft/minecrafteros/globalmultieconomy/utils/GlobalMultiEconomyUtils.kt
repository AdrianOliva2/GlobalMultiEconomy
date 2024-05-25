package net.serveminecraft.minecrafteros.globalmultieconomy.utils

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.globalmultieconomy.GlobalMultiEconomy
import net.serveminecraft.minecrafteros.perworldeconomy.utils.MessagesUtil
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class GlobalMultiEconomyUtils {
    companion object {
        fun help(sender: CommandSender) {
            val plugin: GlobalMultiEconomy = GlobalMultiEconomy.getInstance()
            val messages: FileConfiguration = plugin.getMessages()
            val replaces: MutableMap<String, String> = mutableMapOf("%prefix%" to plugin.prefix)

            replaces["%version%"] = plugin.pluginVersion
            replaces["%author%"] = plugin.author
            val messagesList: MutableList<String> = MessagesUtil.getFullStringListFromConfig(messages, "help", replaces)
            replaces.remove("%version%")
            replaces.remove("%author%")
            for (message: String in messagesList) {
                sender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message))
            }
        }

        fun helpPlayer(player: Player) {
            val plugin: GlobalMultiEconomy = GlobalMultiEconomy.getInstance()
            val messages: FileConfiguration = plugin.getMessages()
            val replaces: MutableMap<String, String> = mutableMapOf("%prefix%" to plugin.prefix)

            if (player.isOp || player.hasPermission("globalmultieconomy.admin")) {
                help(player)
            } else {
                val message: String = MessagesUtil.getFullStringFromConfig(messages, "no-permission", replaces)
                player.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message))
            }
        }
    }
}