package net.serveminecraft.minecrafteros.globalmultieconomy.db

class DBNotConnectedException: Exception() {

    override val message: String
        get() = "Not connected to database, first connect to database using DBManager.connect()"

}