package net.serveminecraft.minecrafteros.globalmultieconomy.db

class DBNotRegistriesFoundByPlayerAndEconomy : Exception() {
    override val message: String
        get() = "No registries found for player and economy."
}
