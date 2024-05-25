CREATE DATABASE IF NOT EXISTS `globalmultieconomy` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;
USE `globalmultieconomy`;

CREATE TABLE IF NOT EXISTS economy
(
    economyName VARCHAR(30) NOT NULL UNIQUE PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS player
(
    playerUUID VARCHAR(36) NOT NULL UNIQUE PRIMARY KEY,
    playerName varchar(30) NOT NULL
);

CREATE TABLE IF NOT EXISTS player_economy
(
    playerUUID  VARCHAR(36) NOT NULL UNIQUE,
    economyName VARCHAR(30) NOT NULL,
    balance     DOUBLE      NOT NULL,
    PRIMARY KEY (playerUUID, economyName),
    CONSTRAINT `playerEconomy_playerUUID` FOREIGN KEY (`playerUUID`) REFERENCES `player` (`playerUUID`),
    CONSTRAINT `playerEconomy_economyName` FOREIGN KEY (`economyName`) REFERENCES `economy` (`economyName`)
);

CREATE TABLE IF NOT EXISTS transactions
(
    id          INT                                     NOT NULL AUTO_INCREMENT,
    performedBy VARCHAR(36)                             NOT NULL,
    playerUUID  VARCHAR(36)                             NOT NULL,
    economyName VARCHAR(30)                             NOT NULL,
    amount      DOUBLE                                  NOT NULL,
    type        ENUM ('ADD', 'REMOVE', 'SET', 'CREATE') NOT NULL,
    time        timestamp                               NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT `transactions_playerUUID` FOREIGN KEY (`playerUUID`) REFERENCES `player` (`playerUUID`),
    CONSTRAINT `transactions_economyName` FOREIGN KEY (`economyName`) REFERENCES `economy` (`economyName`)
);