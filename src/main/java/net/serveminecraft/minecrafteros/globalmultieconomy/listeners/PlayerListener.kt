package net.serveminecraft.minecrafteros.globalmultieconomy.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.globalmultieconomy.GlobalMultiEconomy
import net.serveminecraft.minecrafteros.globalmultieconomy.db.DBManager
import net.serveminecraft.minecrafteros.perworldeconomy.utils.MessagesUtil
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class PlayerListener : Listener {

    val plugin: GlobalMultiEconomy = GlobalMultiEconomy.getInstance()

    private fun createPlayerEconomyIfnotexists(playerUUID: UUID, playerName: String, balance: Double) {
        val economies: Set<String> = DBManager.getEconomies()
        economies.forEach { economy ->
            if (!DBManager.playerHaveBalanceInEconomy(playerUUID, economy))
                DBManager.setUserBalance(playerUUID, playerName, economy, balance)
        }
    }

    @EventHandler
    fun onPlayerJoin(e: AsyncPlayerPreLoginEvent) {
        createPlayerEconomyIfnotexists(e.uniqueId, e.name, 0.0)
    }

    @EventHandler
    fun onPlayerMoveWorld(e: PlayerChangedWorldEvent) {
        createPlayerEconomyIfnotexists(e.player.uniqueId, e.player.name, 0.0)
    }

    @EventHandler
    fun onPlayerQuit(e: PlayerQuitEvent) {
        createPlayerEconomyIfnotexists(e.player.uniqueId, e.player.name, 0.0)
    }

    @EventHandler
    fun onPlayerChat(e: AsyncChatEvent) {
        if (plugin.requestDeleteEconomyNameConfirm.isEmpty()) return
        val messageTextComponent: TextComponent = e.message() as TextComponent
        if (messageTextComponent.content().equals("DeLeTe ${plugin.requestDeleteEconomyNameConfirm}", false)) {
            if (DBManager.deleteEconomy(plugin.requestDeleteEconomyNameConfirm)) {
                e.player.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                        MessagesUtil.getFullStringFromConfig(
                            plugin.getMessages(),
                            "economy-deleted",
                            mutableMapOf(
                                "%prefix%" to plugin.prefix,
                                "%economy%" to plugin.requestDeleteEconomyNameConfirm
                            )
                        )
                    )
                )
            } else {
                e.player.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand().deserialize(
                        MessagesUtil.getFullStringFromConfig(
                            plugin.getMessages(),
                            "economy-not-found",
                            mutableMapOf(
                                "%prefix%" to plugin.prefix,
                                "%economy%" to plugin.requestDeleteEconomyNameConfirm
                            )
                        )
                    )
                )
            }
            plugin.requestDeleteEconomyNameConfirm = ""
            e.isCancelled = true
            return
        } else {
            plugin.requestDeleteEconomyNameConfirm = ""
        }
    }

}