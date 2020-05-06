/*
Navicat MySQL Data Transfer

Source Server         : jst - r
Source Server Version : 50708
Source Host           : 120.77.210.128:7012
Source Database       : hospital

Target Server Type    : MYSQL
Target Server Version : 50708
File Encoding         : 65001

Date: 2018-12-31 13:01:07
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for prescription
-- ----------------------------
DROP TABLE IF EXISTS `prescription`;
CREATE TABLE `prescription` (
  `listid` int(11) NOT NULL,
  `medicineid` int(11) NOT NULL,
  `itemCode` varchar(20) DEFAULT NULL,
  `itemGrade` varchar(10) DEFAULT NULL,
  `medname` varchar(45) DEFAULT NULL,
  `medname2` varchar(45) DEFAULT NULL,
  `inprice` decimal(12,2) DEFAULT '0.00',
  `outprice` decimal(12,2) DEFAULT '0.00',
  `type` varchar(45) DEFAULT NULL,
  `unitName` varchar(16) DEFAULT NULL,
  `factoryName` varchar(255) DEFAULT NULL,
  `feetypeName` varchar(255) DEFAULT NULL,
  `number` decimal(12,2) DEFAULT '0.00',
  KEY `med_idx` (`medicineid`),
  KEY `pr_idx` (`listid`),
  CONSTRAINT `li` FOREIGN KEY (`listid`) REFERENCES `prescriptionlist` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
