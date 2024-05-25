package net.serveminecraft.minecrafteros.perworldeconomy.utils

import org.bukkit.configuration.file.FileConfiguration

class MessagesUtil {

    companion object {
        fun getFullStringFromConfig(configFile: FileConfiguration, stringPath: String, replaces: Map<String, String>): String {
            val configMessage: String? = configFile.getString(stringPath, "")
            if (configMessage != null) {
                var replacedMessage: String = configMessage.toString()
                for (replace: String in replaces.keys) {
                    replacedMessage = replacedMessage.replace(replace, replaces.getValue(replace))
                }
                return replacedMessage
            }
            return ""
        }

        fun getFullStringListFromConfig(configFile: FileConfiguration, stringPath: String, replaces: Map<String, String>): MutableList<String> {
            val configMessages: MutableList<String> = configFile.getStringList(stringPath)
            for (i in 0..<configMessages.size) {
                for (replace: String in replaces.keys) {
                    configMessages[i] = configMessages.elementAt(i).replace(replace, replaces.getValue(replace))
                }
            }
            return configMessages
        }
    }
}