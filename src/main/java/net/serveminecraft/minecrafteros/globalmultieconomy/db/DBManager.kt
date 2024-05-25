package net.serveminecraft.minecrafteros.globalmultieconomy.db

import net.serveminecraft.minecrafteros.globalmultieconomy.GlobalMultiEconomy
import org.bukkit.configuration.file.FileConfiguration
import java.sql.*
import java.util.*

// TODO: IMPORTANTE Revisar el correcto funcionamiento de TODOS los métodos

class DBManager {
    companion object {
        private val plugin: GlobalMultiEconomy = GlobalMultiEconomy.getInstance()
        private var config: FileConfiguration = plugin.config

        private var HOST: String = config.getString("DB.host", "127.0.0.1")!!
        private var PORT: Int = config.getInt("DB.port", 3306)
        private var USER: String = config.getString("DB.user", "root")!!
        private var PASSWORD: String = config.getString("DB.password", "")!!
        private var DB: String = config.getString("DB.database", "globalmultieconomy")!!
        private var CONNECTION_URL: String = "jdbc:mysql://$HOST:$PORT/$DB"
        private const val DRIVER: String = "com.mysql.cj.jdbc.Driver"
        private var connection: Connection? = null

        private fun isConected(): Boolean {
            return connection != null && connection!!.isValid(200) && !connection!!.isClosed
        }

        private fun createDBIfNotExists() {
            try {
                val newConnection = DriverManager.getConnection("jdbc:mysql://$HOST:$PORT", USER, PASSWORD)
                val statement: Statement? = newConnection?.createStatement()
                statement?.execute("CREATE DATABASE IF NOT EXISTS `$DB` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;")
                statement?.execute("USE `$DB`")
                statement?.execute("CREATE TABLE IF NOT EXISTS economy (economyName VARCHAR(30) NOT NULL UNIQUE PRIMARY KEY);")
                statement?.execute("CREATE TABLE IF NOT EXISTS player (playerUUID VARCHAR(36) NOT NULL UNIQUE PRIMARY KEY, playerName varchar(30) NOT NULL);")
                statement?.execute("CREATE TABLE IF NOT EXISTS player_economy (playerUUID VARCHAR(36) NOT NULL UNIQUE, economyName VARCHAR(30) NOT NULL, balance DOUBLE NOT NULL, PRIMARY KEY (playerUUID, economyName), CONSTRAINT `playerEconomy_playerUUID` FOREIGN KEY (`playerUUID`) REFERENCES `player` (`playerUUID`), CONSTRAINT `playerEconomy_economyName` FOREIGN KEY (`economyName`) REFERENCES `economy` (`economyName`));")
                statement?.execute("CREATE TABLE IF NOT EXISTS transactions (id INT NOT NULL AUTO_INCREMENT, performedBy VARCHAR(36) NOT NULL, playerUUID VARCHAR(36) NOT NULL, economyName VARCHAR(30) NOT NULL, amount DOUBLE NOT NULL, type ENUM('ADD', 'REMOVE', 'SET', 'CREATE') NOT NULL, time timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, PRIMARY KEY (id), CONSTRAINT `transactions_playerUUID` FOREIGN KEY (`playerUUID`) REFERENCES `player` (`playerUUID`), CONSTRAINT `transactions_economyName` FOREIGN KEY (`economyName`) REFERENCES `economy` (`economyName`));")
                statement?.close()
                newConnection?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun connect(): Boolean {
            try {
                Class.forName(DRIVER)
                connection = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD)
            } catch (e: SQLSyntaxErrorException) {
                val createDBInHostIfNotExists = config.getBoolean("DB.create-db-in-host-ifnot-exist", false)
                if (e.message.equals("Unknown database '$DB'", false) && createDBInHostIfNotExists) {
                    connection?.close()
                    createDBIfNotExists()
                    connect()
                    return true
                } else {
                    throw e
                }
            } catch (e: Exception) {
                throw e
            }
            return false
        }

        fun changeDBConnectionFromConfig(): String {
            config = plugin.config
            val newHOST: String = config.getString("DB.host", "127.0.0.1")!!
            val newPORT: Int = config.getInt("DB.port", 3306)
            val newUSER: String = config.getString("DB.user", "root")!!
            val newPASSWORD: String = config.getString("DB.password", "")!!
            val newDB: String = config.getString("DB.database", "globalmultieconomy")!!
            if (HOST != newHOST || PORT != newPORT || USER != newUSER || PASSWORD != newPASSWORD || DB != newDB) {
                try {
                    disconnect()
                    HOST = newHOST
                    PORT = newPORT
                    USER = newUSER
                    PASSWORD = newPASSWORD
                    DB = newDB
                    CONNECTION_URL = "jdbc:mysql://$HOST:$PORT/$DB"
                    val createdDB: Boolean = connect()
                    return if (createdDB) {
                        "CREATED $HOST:$PORT/$DB"
                    } else {
                        "$HOST:$PORT/$DB"
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
            return ""
        }

        fun createEconomy(economyName: String): Boolean {
            if (!isConected()) return false
            try {
                val statement: PreparedStatement? =
                    connection?.prepareStatement("INSERT INTO economy (economyName) VALUES (?)")
                statement?.setString(1, economyName)
                statement?.executeUpdate()
                statement?.close()
                return true
            } catch (e: SQLIntegrityConstraintViolationException) {
                return false
            } catch (e: Exception) {
                return false
            }
        }

        fun deleteEconomy(economyName: String): Boolean {
            if (!isConected()) return false

            try {
                connection?.prepareStatement("DELETE FROM player_economy WHERE economyName = ?").use { stmt1 ->
                    stmt1?.setString(1, economyName)
                    stmt1?.executeUpdate()
                    stmt1?.close()
                }

                connection?.prepareStatement("DELETE FROM transactions WHERE economyName = ?").use { stmt2 ->
                    stmt2?.setString(1, economyName)
                    stmt2?.executeUpdate()
                    stmt2?.close()
                }

                connection?.prepareStatement("DELETE FROM economy WHERE economyName = ?").use { stmt3 ->
                    stmt3?.setString(1, economyName)
                    stmt3?.executeUpdate()
                    stmt3?.close()
                }

                return true
            } catch (e: SQLIntegrityConstraintViolationException) {
                return false
            }
        }

        fun getEconomies(): Set<String> {
            if (!isConected()) throw DBNotConnectedException()
            try {
                val statement: Statement? = connection?.createStatement()
                val resultSet: ResultSet? = statement?.executeQuery("SELECT DISTINCT economyName FROM economy")
                val economies: MutableSet<String> = mutableSetOf()
                if (resultSet != null) {
                    while (resultSet.next()) {
                        economies.add(resultSet.getString("economyName"))
                    }
                    resultSet.close()
                    statement.close()
                }
                return economies
            } catch (e: Exception) {
                throw e
            }
        }

        fun economyExists(economyName: String): Boolean {
            if (!isConected()) return false

            var statement: PreparedStatement? = null
            try {
                statement = connection?.prepareStatement("SELECT economyName FROM economy WHERE economyName = ?")
                statement?.setString(1, economyName)
                val result: ResultSet? = statement?.executeQuery()
                return result != null && result.next()
            } catch (e: SQLException) {
                return false
            } finally {
                statement?.close()
            }
        }

        fun getPlayerUUIDByName(playerName: String): UUID? {
            if (!isConected()) throw DBNotConnectedException()
            try {
                val statement: PreparedStatement? =
                    connection?.prepareStatement("SELECT playerUUID FROM player WHERE playerName = ?")
                statement?.setString(1, playerName)
                val resultSet = statement?.executeQuery()
                if (resultSet != null) {
                    resultSet.next()
                    val playerUUID: UUID = UUID.fromString(resultSet.getString("playerUUID"))
                    resultSet.close()
                    statement.close()
                    return playerUUID
                }
            } catch (e: Exception) {
                return null
            }
            return null
        }

        fun getPlayerAndEconomy(playerUUID: UUID, economyName: String): ResultSet? {
            if (!isConected()) throw DBNotConnectedException()
            try {
                val statement: PreparedStatement? =
                    connection?.prepareStatement("SELECT p.playerUUID, pe.economyName FROM player AS p INNER JOIN player_economy AS pe ON p.playerUUID = pe.playerUUID WHERE p.playerUUID = ? AND pe.economyName = ?")
                statement?.setString(1, playerUUID.toString())
                statement?.setString(2, economyName)
                return statement?.executeQuery()
            } catch (e: Exception) {
                throw e
            }
        }

        fun playerHaveBalanceInEconomy(playerUUID: UUID, economyName: String): Boolean {
            if (!isConected()) throw DBNotConnectedException()
            try {
                val resultSet = getPlayerAndEconomy(playerUUID, economyName)
                if (resultSet != null) {
                    resultSet.next()
                    if (resultSet.getString("playerUUID") == playerUUID.toString() && resultSet.getString("economyName") == economyName) {
                        val statement: Statement? = resultSet.statement
                        resultSet.close()
                        statement?.close()
                        return true
                    }
                }
            } catch (e: Exception) {
                return false
            }
            return false
        }

        fun getUserBalance(playerUUID: UUID, economyName: String): Double {
            if (!isConected()) throw DBNotConnectedException()
            try {
                val statement: PreparedStatement? =
                    connection?.prepareStatement("SELECT balance FROM player_economy WHERE playerUUID = ? AND economyName = ?")
                statement?.setString(1, playerUUID.toString())
                statement?.setString(2, economyName)
                val resultSet = statement?.executeQuery()
                if (resultSet != null) {
                    resultSet.next()
                    val balance: Double = resultSet.getDouble("balance")
                    resultSet.close()
                    statement.close()
                    return balance
                }
            } catch (e: Exception) {
                throw e
            }
            return 0.0
        }

        private fun createUserEconomyIfNotExists(
            playerUUID: UUID,
            playerName: String?,
            economyName: String,
            balance: Double,
            performedBy: String = "CONSOLE"
        ) {
            if (!isConected()) throw DBNotConnectedException()
            if (!economyExists(economyName)) throw DBEconomyNotExistsException()
            try {
                if (!playerHaveBalanceInEconomy(playerUUID, economyName)) {
                    if (playerName.isNullOrEmpty()) throw DBPlayerNotIntroducedWhenPlayerNoHaveBalanceException()

                    if (getPlayerUUIDByName(playerName) == null) {
                        val statement: PreparedStatement? =
                            connection?.prepareStatement("INSERT INTO player (playerUUID, playerName) VALUES (?, ?)")
                        statement?.setString(1, playerUUID.toString())
                        statement?.setString(2, playerName)
                        statement?.executeUpdate()
                        statement?.close()
                    }

                    var statement: PreparedStatement? =
                        connection?.prepareStatement("INSERT INTO player_economy (playerUUID, economyName, balance) VALUES (?, ?, ?)")
                    statement?.setString(1, playerUUID.toString())
                    statement?.setString(2, economyName)
                    statement?.setDouble(3, balance)
                    statement?.executeUpdate()

                    statement?.close()
                    statement =
                        connection?.prepareStatement("INSERT INTO transactions (performedBy, playerUUID, economyName, amount, type) VALUES (?, ?, ?, ?, ?)")
                    statement?.setString(1, performedBy)
                    statement?.setString(2, playerUUID.toString())
                    statement?.setString(3, economyName)
                    statement?.setDouble(4, balance)
                    statement?.setString(5, "CREATE")
                    statement?.executeUpdate()
                    statement?.close()
                } else {
                    var statement: PreparedStatement? =
                        connection?.prepareStatement("UPDATE player_economy SET balance = ? WHERE playerUUID = ? AND economyName = ?")
                    statement?.setDouble(1, balance)
                    statement?.setString(2, playerUUID.toString())
                    statement?.setString(3, economyName)
                    statement?.executeUpdate()
                    statement?.close()

                    statement =
                        connection?.prepareStatement("INSERT INTO transactions (performedBy, playerUUID, economyName, amount, type) VALUES (?, ?, ?, ?, ?)")
                    statement?.setString(1, performedBy)
                    statement?.setString(2, playerUUID.toString())
                    statement?.setString(3, economyName)
                    statement?.setDouble(4, balance)
                    statement?.setString(5, "SET")
                    statement?.executeUpdate()
                    statement?.close()
                }
            } catch (e: Exception) {
                throw e
            }
        }

        fun setUserBalance(playerUUID: UUID, economyName: String, balance: Double, performedBy: String = "CONSOLE") {
            createUserEconomyIfNotExists(playerUUID, null, economyName, balance, performedBy)
        }

        fun setUserBalance(
            playerUUID: UUID,
            playerName: String,
            economyName: String,
            balance: Double,
            performedBy: String = "CONSOLE"
        ) {
            createUserEconomyIfNotExists(playerUUID, playerName, economyName, balance, performedBy)
        }

        fun removeUserBalance(playerUUID: UUID, economyName: String, balance: Double, performedBy: String = "CONSOLE") {
            if (!isConected()) throw DBNotConnectedException()

            try {
                val resultSet = getPlayerAndEconomy(playerUUID, economyName)
                if (resultSet != null) {
                    resultSet.next()

                    if (resultSet.getString("playerUUID") == playerUUID.toString() && resultSet.getString("economyName") == economyName) {
                        val statement: Statement? = resultSet.statement
                        resultSet.close()
                        statement?.close()

                        var newStatement: PreparedStatement? =
                            connection?.prepareStatement("UPDATE player_economy SET balance = balance - ? WHERE playerUUID = ? AND economyName = ?")
                        newStatement?.setDouble(1, balance)
                        newStatement?.setString(2, playerUUID.toString())
                        newStatement?.setString(3, economyName)
                        newStatement?.executeUpdate()
                        newStatement?.close()

                        newStatement =
                            connection?.prepareStatement("INSERT INTO transactions (performedBy, playerUUID, economyName, amount, type) VALUES (?, ?, ?, ?, ?)")
                        newStatement?.setString(1, performedBy)
                        newStatement?.setString(2, playerUUID.toString())
                        newStatement?.setString(3, economyName)
                        newStatement?.setDouble(4, balance)
                        newStatement?.setString(5, "REMOVE")
                        newStatement?.executeUpdate()
                        newStatement?.close()
                    } else {
                        throw DBNotRegistriesFoundByPlayerAndEconomy()
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }

        fun addUserBalance(playerUUID: UUID, economyName: String, balance: Double, performedBy: String = "CONSOLE") {
            if (!isConected()) throw DBNotConnectedException()
            try {
                val resultSet = getPlayerAndEconomy(playerUUID, economyName)
                if (resultSet != null) {
                    resultSet.next()

                    if (resultSet.getString("playerUUID") == playerUUID.toString() && resultSet.getString("economyName") == economyName) {
                        val statement: Statement? = resultSet.statement
                        resultSet.close()
                        statement?.close()

                        var newStatement: PreparedStatement? =
                            connection?.prepareStatement("UPDATE player_economy SET balance = balance + ? WHERE playerUUID = ? AND economyName = ?")
                        newStatement?.setDouble(1, balance)
                        newStatement?.setString(2, playerUUID.toString())
                        newStatement?.setString(3, economyName)
                        newStatement?.executeUpdate()
                        newStatement?.close()

                        newStatement =
                            connection?.prepareStatement("INSERT INTO transactions (performedBy, playerUUID, economyName, amount, type) VALUES (?, ?, ?, ?, ?)")
                        newStatement?.setString(1, performedBy)
                        newStatement?.setString(2, playerUUID.toString())
                        newStatement?.setString(3, economyName)
                        newStatement?.setDouble(4, balance)
                        newStatement?.setString(5, "ADD")
                        newStatement?.executeUpdate()
                        newStatement?.close()
                    } else {
                        throw DBNotRegistriesFoundByPlayerAndEconomy()
                    }
                }
            } catch (e: Exception) {
                throw e
            }
        }

        fun disconnect(): String {
            if (!isConected()) throw DBNotConnectedException()
            try {
                connection?.close()
                return "Disconnected from database"
            } catch (e: Exception) {
                throw e
            }
        }
    }
}