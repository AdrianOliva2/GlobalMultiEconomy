package net.serveminecraft.minecrafteros.globalmultieconomy

import me.clip.placeholderapi.PlaceholderAPIPlugin
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.serveminecraft.minecrafteros.globalmultieconomy.commands.EconomyCommands
import net.serveminecraft.minecrafteros.globalmultieconomy.commands.GlobalMultiEconomyCommand
import net.serveminecraft.minecrafteros.globalmultieconomy.configs.CustomConfig
import net.serveminecraft.minecrafteros.globalmultieconomy.db.DBManager
import net.serveminecraft.minecrafteros.globalmultieconomy.listeners.PlayerListener
import net.serveminecraft.minecrafteros.globalmultieconomy.papiexpansions.EconomyPlaceHolderExpansion
import net.serveminecraft.minecrafteros.globalmultieconomy.tabcompleters.GlobalMultiEconomyTabCompleter
import org.bukkit.command.PluginCommand
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.PluginDescriptionFile
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class GlobalMultiEconomy : JavaPlugin() {

    companion object {
        fun getInstance(): GlobalMultiEconomy {
            return getPlugin(GlobalMultiEconomy::class.java)
        }
    }

    @Suppress("DEPRECATION")
    private val descriptionFile: PluginDescriptionFile = this.description
    private val pluginName: String = descriptionFile.name
    val pluginVersion: String = descriptionFile.version
    val author: String = descriptionFile.authors[0]
    private lateinit var messages: CustomConfig
    var prefix: String = ""
    var placeHolderAPI: PlaceholderAPIPlugin? = null
    var requestDeleteEconomyNameConfirm: String = ""

    override fun onEnable() {
        registerConfig()
        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            placeHolderAPI = server.pluginManager.getPlugin("PlaceholderAPI") as PlaceholderAPIPlugin?
            EconomyPlaceHolderExpansion().register()
            server.consoleSender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&8[&b$pluginName&8] &aPlaceholderAPI found, registering plugin placeholders!")
            )
        } else {
            server.consoleSender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&8[&b$pluginName&8] &cPlaceholderAPI not found, plugin placeholders will not work for other plugins!")
            )
        }
        try {
            server.consoleSender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&8[&b$pluginName&8] &7Connecting to database...")
            )
            if (DBManager.connect()) {
                val db: String = config.getString("DB.database", "perworldeconomy")!!
                server.consoleSender.sendMessage(
                    LegacyComponentSerializer.legacyAmpersand()
                        .deserialize("&8[&b$pluginName&8] &7Created database &e$db &7because it didn't exist and option create db if not exists is &etrue &7in &econfig.yml&7.")
                )
            }
        } catch (e: Exception) {
            this.logger.severe(e.message)
            server.consoleSender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&8[&b$pluginName&8] &cError connecting to database. Disabling plugin...")
            )
            server.pluginManager.disablePlugin(this)
            return
        }
        server.consoleSender.sendMessage(
            LegacyComponentSerializer.legacyAmpersand().deserialize("&8[&b$pluginName&8] &aConnected to database!")
        )
        registerListeners()
        registerCommands()
        server.consoleSender.sendMessage(
            LegacyComponentSerializer.legacyAmpersand()
                .deserialize("&8[&b$pluginName&8] &aPlugin enabled on version &8[&b$pluginVersion&8]")
        )
    }

    private fun registerConfig() {
        config.options().copyDefaults(true)
        saveDefaultConfig()
        messages = CustomConfig("messages.yml")
        prefix = messages.getConfig().getString("prefix", "")!!
    }

    private fun registerListeners() {
        val pluginManager: PluginManager = server.pluginManager
        pluginManager.registerEvents(PlayerListener(), this)
    }

    private fun registerCommands() {
        val globalMultiEconomyCommand: PluginCommand? = getCommand("globalmultieconomy")
        if (globalMultiEconomyCommand != null) {
            globalMultiEconomyCommand.setExecutor(GlobalMultiEconomyCommand())
            globalMultiEconomyCommand.tabCompleter = GlobalMultiEconomyTabCompleter()
        }
        val economies: Set<String> = DBManager.getEconomies()
        for (economy: String in economies) {
            server.commandMap.knownCommands[economy] = EconomyCommands(economy)
        }
    }

    fun getMessages(): FileConfiguration {
        return messages.getConfig()
    }

    fun reload(): String {
        val configFile = File(dataFolder, "config.yml")
        if (configFile.exists()) {
            reloadConfig()
        } else {
            saveDefaultConfig()
        }
        saveConfig()
        messages.reloadConfig()
        messages.saveConfig()
        prefix = messages.getConfig().getString("prefix", "")!!
        try {
            val newDB: String = DBManager.changeDBConnectionFromConfig()
            if (newDB.isNotEmpty()) {
                val createdDB: List<String> = newDB.split(" ")
                var splittedNewHost = newDB.split("/")
                var message = ""
                if (createdDB.size == 2 && createdDB[0].equals("CREATED", false)) {
                    splittedNewHost = newDB.split(" ")[1].split("/")
                    message += "&7Created database &e${splittedNewHost[1]} &7in &e${splittedNewHost[0]} &7because it didn't exist and option create db if not exists is &etrue &7in &econfig.yml&7."
                    server.consoleSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message))
                }
                if (message.isNotEmpty()) {
                    message += "|&7Database connection changed to &e${splittedNewHost[1]} &7from &e${splittedNewHost[0]}"
                    val consoleMessage: String = message.split("|")[1]
                    server.consoleSender.sendMessage(
                        LegacyComponentSerializer.legacyAmpersand().deserialize(consoleMessage)
                    )
                } else {
                    message += "&7Database connection changed to &e${splittedNewHost[1]} &7from &e${splittedNewHost[0]}"
                    server.consoleSender.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message))
                }
                return message
            }
        } catch (e: Exception) {
            e.printStackTrace()
            this.logger.severe(e.message)
            server.consoleSender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&8[&b$pluginName&8] &cError changing database connection. Disabling plugin...")
            )
            server.pluginManager.disablePlugin(this)
            return ""
        }
        return ""
    }

    override fun onDisable() {
        try {
            server.consoleSender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&8[&b$pluginName&8] &7Disconnecting from database...")
            )
            DBManager.disconnect()
        } catch (_: Exception) {
        } finally {
            server.consoleSender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&8[&b$pluginName&8] &aDisconnected from database!")
            )
            server.consoleSender.sendMessage(
                LegacyComponentSerializer.legacyAmpersand()
                    .deserialize("&8[&b$pluginName&8] &cPlugin disabled on version &8[&b$pluginVersion&8]")
            )
        }
    }
}
