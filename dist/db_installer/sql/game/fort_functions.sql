CREATE TABLE IF NOT EXISTS `fort_functions` (
  `fort_id` int(2) NOT NULL DEFAULT '0',
  `type` int(1) NOT NULL DEFAULT '0',
  `lvl` int(3) NOT NULL DEFAULT '0',
  `lease` int(10) NOT NULL DEFAULT '0',
  `rate` decimal(20,0) NOT NULL DEFAULT '0',
  `endTime` bigint(13) unsigned NOT NULL DEFAULT '0',
  PRIMARY KEY (`fort_id`,`type`)
) DEFAULT CHARSET=latin1 COLLATE=latin1_general_ci;