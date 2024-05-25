package net.serveminecraft.minecrafteros.globalmultieconomy.db

class DBPlayerNotIntroducedWhenPlayerNoHaveBalanceException: Exception() {
    override val message: String
        get() = "Player not introduced when player no have balance in the specified economy."
}
