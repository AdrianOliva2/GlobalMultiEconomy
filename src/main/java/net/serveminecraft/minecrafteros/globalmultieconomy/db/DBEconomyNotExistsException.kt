package net.serveminecraft.minecrafteros.globalmultieconomy.db

class DBEconomyNotExistsException : Exception() {

    override val message: String
        get() = "Economy not exists"

}