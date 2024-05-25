package net.serveminecraft.minecrafteros.globalmultieconomy.papiexpansions

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import net.serveminecraft.minecrafteros.globalmultieconomy.db.DBManager
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.text.DecimalFormat
import java.util.*

class EconomyPlaceHolderExpansion : PlaceholderExpansion() {

    private fun executePlaceHolderRequest(uuid: UUID, args: List<String>): String {
        if (args[0] == "balance") {
            if (args.size == 2) {
                val economy = args[1]
                return DBManager.getUserBalance(uuid, economy).toString()
            }

            if (args.size == 3 && args[2] == "formatted") {
                val economy = args[1]
                val balance: Double = DBManager.getUserBalance(uuid, economy)
                if (balance >= 1000) {
                    val formatter = DecimalFormat("#,###.00")
                    return formatter.format(balance)
                }
                return balance.toString()
            }
        }
        return "invalid_placeholder"
    }

    override fun getIdentifier(): String {
        return "globalmultieconomy"
    }

    override fun getAuthor(): String {
        return "Adrian_oliva"
    }

    override fun getVersion(): String {
        return "1.0.0"
    }

    override fun persist(): Boolean {
        return true
    }

    override fun onPlaceholderRequest(player: Player, identifier: String): String {
        return executePlaceHolderRequest(player.uniqueId, identifier.split("_"))
    }

    override fun onRequest(player: OfflinePlayer, params: String): String? {
        return executePlaceHolderRequest(player.uniqueId, params.split("_"))
    }

}