CREATE TABLE IF NOT EXISTS `bot_reported_char_data` (
	`botId` INT UNSIGNED NOT NULL DEFAULT 0,
	`reporterId` INT UNSIGNED NOT NULL DEFAULT 0,
	`reportDate` BIGINT(13) unsigned NOT NULL DEFAULT '0',
	PRIMARY KEY (`botId`, `reporterId`)
) DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;