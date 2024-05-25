package net.serveminecraft.minecrafteros.globalmultieconomy.commands

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.globalmultieconomy.GlobalMultiEconomy
import net.serveminecraft.minecrafteros.globalmultieconomy.db.DBManager
import net.serveminecraft.minecrafteros.globalmultieconomy.utils.GlobalMultiEconomyUtils
import net.serveminecraft.minecrafteros.perworldeconomy.utils.MessagesUtil
import org.bukkit.OfflinePlayer
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.util.*

class EconomyCommands(economy: String) : Command(economy, "Commands of economy $economy", "", listOf()) {

    val plugin: GlobalMultiEconomy = GlobalMultiEconomy.getInstance()

    override fun tabComplete(sender: CommandSender, alias: String, args: Array<out String>?): MutableList<String> {
        val completers: MutableList<String> = mutableListOf()
        if (args?.size == 1) {
            if (sender !is Player || sender.isOp || sender.hasPermission("globalmultieconomy.economy.admin")) completers.addAll(
                listOf(
                    "give",
                    "take",
                    "set"
                )
            )
            if (sender !is Player || sender.isOp || sender.hasPermission("globalmultieconomy.economy.check.others")) {
                val plugin: GlobalMultiEconomy = GlobalMultiEconomy.getInstance()
                val players: Collection<Player> = plugin.server.onlinePlayers
                players.forEach { player: Player -> completers.add(player.name) }
            }

            if (sender is Player && (sender.isOp || sender.hasPermission("globalmultieconomy.economy.check.self"))) {
                val player: Player = sender
                completers.add(player.name)
            }

            return completers
        }
        if (args?.size == 2 && (args[0] == "give" || args[0] == "take" || args[0] == "set")) {
            if (sender !is Player || sender.isOp || sender.hasPermission("globalmultieconomy.economy.admin")) {
                val plugin: GlobalMultiEconomy = GlobalMultiEconomy.getInstance()
                val players: Collection<Player> = plugin.server.onlinePlayers
                players.forEach { player: Player -> completers.add(player.name) }
                return completers
            }
        }
        if (args?.size == 3 && (args[0] == "give" || args[0] == "take" || args[0] == "set")) {
            if (sender !is Player || sender.isOp || sender.hasPermission("globalmultieconomy.economy.admin")) {
                completers.add("1")
                return completers
            }
        }
        return completers
    }

    private fun checkPlayerBalance(
        playerUUID: UUID,
        playerName: String?,
        messages: FileConfiguration,
        replaces: MutableMap<String, String>,
        sender: CommandSender
    ): Boolean {
        if (sender !is Player || sender.isOp || (sender.uniqueId == playerUUID && sender.hasPermission("globalmultieconomy.economy.check.self")) || (sender.uniqueId != playerUUID && sender.hasPermission(
                "globalmultieconomy.economy.check.others"
            ))
        ) {
            if (!DBManager.playerHaveBalanceInEconomy(playerUUID, label)) {
                replaces["%economy%"] = label
                replaces["%player%"] = playerName.orEmpty()
                sender.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(
                            MessagesUtil.getFullStringFromConfig(
                                messages,
                                "player-yet-not-have-balance-in-economy",
                                replaces
                            )
                        )
                )
                replaces.remove("%economy%")
                replaces.remove("%player%")
                return false
            } else {
                replaces["%player%"] = playerName.orEmpty()
                replaces["%economy%"] = label
                val balance: Double = DBManager.getUserBalance(playerUUID, label)
                if (balance >= 1000) {
                    val formatter = DecimalFormat("#,###.00")
                    replaces["%balance%"] = formatter.format(balance)
                } else {
                    replaces["%balance%"] = balance.toString()
                }
                sender.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(MessagesUtil.getFullStringFromConfig(messages, "balance-others-check", replaces))
                )
                replaces.remove("%player%")
                replaces.remove("%economy%")
                replaces.remove("%balance%")
                return true
            }
        } else {
            sender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(MessagesUtil.getFullStringFromConfig(messages, "no-permission", replaces))
            )
            return false
        }
    }

    override fun execute(sender: CommandSender, label: String, args: Array<out String>?): Boolean {
        val messages: FileConfiguration = plugin.getMessages()
        val replaces: MutableMap<String, String> = mutableMapOf("%prefix%" to plugin.prefix)
        if (args.isNullOrEmpty() && sender !is Player) {
            sender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                    .deserialize(MessagesUtil.getFullStringFromConfig(messages, "only-player", replaces))
            )
            return false
        }

        if (args.isNullOrEmpty() && sender is Player) {
            val player: Player = sender
            return checkPlayerBalance(player.uniqueId, player.name, messages, replaces, sender)
        }

        if (args?.size == 1) {
            val player: OfflinePlayer = plugin.server.getOfflinePlayer(args[0])
            return checkPlayerBalance(player.uniqueId, player.name, messages, replaces, sender)
        }

        if (args?.size == 3) {
            if (sender !is Player || sender.isOp || sender.hasPermission("globalmultieconomy.economy.admin")) {
                try {
                    val player: OfflinePlayer = plugin.server.getOfflinePlayer(args[1])
                    val balance: Double = args[2].toDouble()
                    var performedBy = "CONSOLE"
                    if (sender is Player) {
                        val playerPerformed: Player = sender
                        performedBy = playerPerformed.uniqueId.toString()
                    }
                    if (DBManager.playerHaveBalanceInEconomy(player.uniqueId, label)) {
                        when (args[0]) {
                            "give" -> {
                                DBManager.addUserBalance(player.uniqueId, label, balance, performedBy)
                                replaces["%amount%"] = balance.toString()
                                replaces["%player%"] = player.name.toString()
                                replaces["%economy%"] = label
                                sender.sendMessage(
                                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                                        MessagesUtil.getFullStringFromConfig(
                                            messages,
                                            "balance-others-add",
                                            replaces
                                        )
                                    )
                                )
                                replaces.remove("%amount%")
                                replaces.remove("%player%")
                                replaces.remove("%economy%")
                                return true
                            }

                            "take" -> {
                                DBManager.removeUserBalance(player.uniqueId, label, balance, performedBy)
                                replaces["%amount%"] = balance.toString()
                                replaces["%player%"] = player.name.toString()
                                replaces["%economy%"] = label
                                sender.sendMessage(
                                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                                        MessagesUtil.getFullStringFromConfig(
                                            messages,
                                            "balance-others-remove",
                                            replaces
                                        )
                                    )
                                )
                                replaces.remove("%amount%")
                                replaces.remove("%player%")
                                replaces.remove("%economy%")
                                return true
                            }

                            "set" -> {
                                DBManager.setUserBalance(player.uniqueId, label, balance, performedBy)
                                replaces["%amount%"] = balance.toString()
                                replaces["%player%"] = player.name.toString()
                                replaces["%economy%"] = label
                                sender.sendMessage(
                                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                                        MessagesUtil.getFullStringFromConfig(
                                            messages,
                                            "balance-others-set",
                                            replaces
                                        )
                                    )
                                )
                                replaces.remove("%amount%")
                                replaces.remove("%player%")
                                replaces.remove("%economy%")
                                return true
                            }

                            else -> {
                                if (sender !is Player) GlobalMultiEconomyUtils.help(sender)
                                else GlobalMultiEconomyUtils.helpPlayer(sender)
                                return false
                            }
                        }
                    } else {
                        replaces["%economy%"] = label
                        sender.sendMessage(
                            LegacyComponentSerializer.legacyAmpersand()
                                .deserialize(
                                    MessagesUtil.getFullStringFromConfig(
                                        messages,
                                        "player-yet-not-have-balance-in-economy",
                                        replaces
                                    )
                                )
                        )
                        replaces.remove("%economy%")
                        return false
                    }
                } catch (e: NumberFormatException) {
                    sender.sendMessage(
                        LegacyComponentSerializer.legacyAmpersand()
                            .deserialize(MessagesUtil.getFullStringFromConfig(messages, "invalid-amount", replaces))
                    )
                    return false
                }
            } else {
                sender.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(MessagesUtil.getFullStringFromConfig(messages, "no-permission", replaces))
                )
                return false
            }
        }
        return false
    }

}