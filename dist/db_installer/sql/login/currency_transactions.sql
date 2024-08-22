CREATE TABLE IF NOT EXISTS `currency_transactions` (
    `transaction_id` INT UNSIGNED NOT NULL AUTO_INCREMENT,
    `account_name` VARCHAR(45) NOT NULL,
    `amount` DECIMAL(15, 2) NOT NULL,
    `transaction_type` ENUM('IN_GAME', 'DONATION', 'PURCHASE') NOT NULL,
    `description` VARCHAR(255),
    `transaction_date` TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`transaction_id`),
    FOREIGN KEY (`account_name`) REFERENCES `accounts` (`login`) ON DELETE CASCADE
) DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
