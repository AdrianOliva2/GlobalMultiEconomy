package net.serveminecraft.minecrafteros.globalmultieconomy.configs

import net.serveminecraft.minecrafteros.globalmultieconomy.GlobalMultiEconomy
import org.bukkit.Bukkit
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.logging.Level

class CustomConfig(private val configFileName: String) {

    private val plugin: GlobalMultiEconomy = GlobalMultiEconomy.getInstance()
    private var dataConfig: FileConfiguration? = null
    private var configFile: File? = null

    init {
        saveDefaultConfig()
    }

    fun reloadConfig() {
        if (configFile == null)
            configFile = File(plugin.dataFolder, configFileName)

        if (!configFile!!.exists()) {
            plugin.saveResource(configFileName, false)
        }

        dataConfig = YamlConfiguration.loadConfiguration(configFile!!)
        val defaultStream: InputStream? = plugin.getResource(configFileName)
        if (defaultStream != null) {
            val defaultConfig: YamlConfiguration = YamlConfiguration.loadConfiguration(InputStreamReader(defaultStream))
            dataConfig?.setDefaults(defaultConfig)

        }
    }

    fun getConfig(): FileConfiguration {
        if (dataConfig == null)
            reloadConfig()
        return dataConfig!!
    }

    fun saveConfig() {
        if (dataConfig == null || configFile == null)
            return
        try {
            getConfig().save(configFile!!)
        } catch (e: IOException) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not save config to $configFile", e)
        }
    }

    private fun saveDefaultConfig() {
        if (configFile == null)
            configFile = File(plugin.dataFolder, configFileName)

        if (!configFile!!.exists()) {
            plugin.saveResource(configFileName, false)
        }
    }

}