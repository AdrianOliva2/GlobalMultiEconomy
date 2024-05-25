package net.serveminecraft.minecrafteros.globalmultieconomy.tabcompleters

import net.serveminecraft.minecrafteros.globalmultieconomy.db.DBManager
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class GlobalMultiEconomyTabCompleter : TabCompleter {
    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): MutableList<String> {
        if (args?.size == 1) {
            return mutableListOf("help", "reload", "economy")
        }
        if (args?.size == 2 && args[0] == "economy") {
            return mutableListOf("create", "delete")
        }
        if (args?.size == 3 && args[0] == "economy" && (args[1] == "delete" || args[1] == "create")) {
            return if (args[1] == "delete") {
                DBManager.getEconomies().toMutableList()
            } else {
                mutableListOf("economyName")
            }
        }
        return mutableListOf()
    }
}