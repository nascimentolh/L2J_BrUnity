CREATE TABLE IF NOT EXISTS `territory_registrations` (
  `castleId` int(1) NOT NULL DEFAULT '0',
  `registeredId` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`castleId`,`registeredId`)
) DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;