package net.serveminecraft.minecrafteros.globalmultieconomy.commands

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.globalmultieconomy.GlobalMultiEconomy
import net.serveminecraft.minecrafteros.globalmultieconomy.db.DBManager
import net.serveminecraft.minecrafteros.globalmultieconomy.utils.GlobalMultiEconomyUtils
import net.serveminecraft.minecrafteros.perworldeconomy.utils.MessagesUtil
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player

class GlobalMultiEconomyCommand : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val plugin: GlobalMultiEconomy = GlobalMultiEconomy.getInstance()
        val replaces: MutableMap<String, String> = mutableMapOf("%prefix%" to plugin.prefix)
        val messages: FileConfiguration = plugin.getMessages()

        if (args != null && args.size == 1 && (args[0] == "help" || args[0] == "reload")) {
            if (args[0] == "help") {
                if (sender is Player) {
                    GlobalMultiEconomyUtils.helpPlayer(sender)
                    return true
                } else {
                    GlobalMultiEconomyUtils.help(sender)
                    return true
                }
            } else {
                if (sender !is Player || sender.isOp || sender.hasPermission("globalmultieconomy.admin")) {
                    plugin.reload()
                    replaces["%prefix%"] = plugin.prefix
                    sender.sendMessage(
                        LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(MessagesUtil.getFullStringFromConfig(messages, "config-reloaded", replaces))
                    )
                    return true
                } else {
                    sender.sendMessage(
                        LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(MessagesUtil.getFullStringFromConfig(messages, "no-permission", replaces))
                    )
                    return false
                }
            }
        }

        if (args != null && args.size == 3 && args[0] == "economy" && (args[1] == "create" || args[1] == "delete")) {
            if (sender !is Player || sender.isOp || sender.hasPermission("globalmultieconomy.admin")) {
                val economy: String = args[2]
                if (args[1] == "create") {
                    if (DBManager.createEconomy(economy)) {
                        plugin.server.commandMap.knownCommands[economy] = EconomyCommands(economy)
                        replaces["%economy%"] = economy
                        sender.sendMessage(
                            LegacyComponentSerializer.legacyAmpersand().deserialize(
                                MessagesUtil.getFullStringFromConfig(
                                    messages,
                                    "economy-created",
                                    replaces
                                )
                            )
                        )
                        replaces.remove("%economy%")
                        return true
                    } else {
                        replaces["%economy%"] = economy
                        sender.sendMessage(
                            LegacyComponentSerializer.legacyAmpersand().deserialize(
                                MessagesUtil.getFullStringFromConfig(
                                    messages,
                                    "economy-already-exists",
                                    replaces
                                )
                            )
                        )
                        replaces.remove("%economy%")
                        return false
                    }

                }
                if (args[1] == "delete") {
                    if (DBManager.getEconomies().contains(economy)) {
                        plugin.requestDeleteEconomyNameConfirm = economy
                        replaces["%economy%"] = economy
                        sender.sendMessage(
                            LegacyComponentSerializer.legacyAmpersand().deserialize(
                                MessagesUtil.getFullStringFromConfig(
                                    messages,
                                    "economy-confirm-delete",
                                    replaces
                                )
                            )
                        )
                        replaces.remove("%economy%")
                        return true
                    } else {
                        replaces["%economy%"] = economy
                        sender.sendMessage(
                            LegacyComponentSerializer.legacyAmpersand().deserialize(
                                MessagesUtil.getFullStringFromConfig(
                                    messages,
                                    "economy-not-found",
                                    replaces
                                )
                            )
                        )
                        replaces.remove("%economy%")
                        return false
                    }
                }
            } else {
                sender.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(MessagesUtil.getFullStringFromConfig(messages, "no-permission", replaces))
                )
                return false
            }
        }

        if (sender !is Player) GlobalMultiEconomyUtils.help(sender)
        else GlobalMultiEconomyUtils.helpPlayer(sender)

        return false
    }

}