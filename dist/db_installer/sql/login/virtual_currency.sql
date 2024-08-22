CREATE TABLE IF NOT EXISTS `virtual_currency` (
    `id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `account_name` VARCHAR(45) NOT NULL,
    `currency_balance` DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    `last_transaction` TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `account_name_UNIQUE` (`account_name`),
    FOREIGN KEY (`account_name`) REFERENCES `accounts` (`login`) ON DELETE CASCADE
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;