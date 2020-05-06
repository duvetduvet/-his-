/*
Navicat MySQL Data Transfer

Source Server         : local
Source Server Version : 50724
Source Host           : localhost:3306
Source Database       : hospital2

Target Server Type    : MYSQL
Target Server Version : 50724
File Encoding         : 65001

Date: 2018-12-30 18:45:06
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for chargerecord
-- ----------------------------
DROP TABLE IF EXISTS `chargerecord`;
CREATE TABLE `chargerecord` (
  `id` int(10) unsigned zerofill NOT NULL AUTO_INCREMENT,
  `card` int(10) unsigned NOT NULL,
  `money` decimal(10,2) NOT NULL,
  `remain` decimal(10,2) NOT NULL,
  `present` decimal(10,2) NOT NULL,
  `date` datetime(6) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `cardid_idx` (`card`),
  CONSTRAINT `v` FOREIGN KEY (`card`) REFERENCES `vip` (`id`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of chargerecord
-- ----------------------------
INSERT INTO `chargerecord` VALUES ('0000000001', '1', '5000.00', '5000.00', '500.00', '2018-12-27 20:12:07.341000');

-- ----------------------------
-- Table structure for countrecord
-- ----------------------------
DROP TABLE IF EXISTS `countrecord`;
CREATE TABLE `countrecord` (
  `id` int(11) NOT NULL,
  `date` date NOT NULL,
  `positionId` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `cr_idx` (`positionId`),
  CONSTRAINT `cr` FOREIGN KEY (`positionId`) REFERENCES `position` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `scr` FOREIGN KEY (`id`) REFERENCES `stockrecord` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of countrecord
-- ----------------------------

-- ----------------------------
-- Table structure for department
-- ----------------------------
DROP TABLE IF EXISTS `department`;
CREATE TABLE `department` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of department
-- ----------------------------
INSERT INTO `department` VALUES ('1', '中药科');
INSERT INTO `department` VALUES ('2', '按摩科');
INSERT INTO `department` VALUES ('3', '脑科');

-- ----------------------------
-- Table structure for doctor
-- ----------------------------
DROP TABLE IF EXISTS `doctor`;
CREATE TABLE `doctor` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `sex` varchar(45) NOT NULL,
  `birth` date DEFAULT NULL,
  `identity` varchar(45) DEFAULT NULL,
  `phone` varchar(45) DEFAULT NULL,
  `address` varchar(45) DEFAULT NULL,
  `departmentId` int(11) NOT NULL,
  `useful` int(11) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `identity_UNIQUE` (`identity`),
  KEY `department_idx` (`departmentId`),
  CONSTRAINT `department` FOREIGN KEY (`departmentId`) REFERENCES `department` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of doctor
-- ----------------------------
INSERT INTO `doctor` VALUES ('7', '张三', '男', null, null, null, null, '1', '1');
INSERT INTO `doctor` VALUES ('8', '李四', '男', '2018-07-03', null, null, null, '2', '1');

-- ----------------------------
-- Table structure for factory
-- ----------------------------
DROP TABLE IF EXISTS `factory`;
CREATE TABLE `factory` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of factory
-- ----------------------------
INSERT INTO `factory` VALUES ('1', '厚德医药有限公司');
INSERT INTO `factory` VALUES ('2', '致和堂医药有限公司');
INSERT INTO `factory` VALUES ('3', '国康医药有限公司');

-- ----------------------------
-- Table structure for feetype
-- ----------------------------
DROP TABLE IF EXISTS `feetype`;
CREATE TABLE `feetype` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of feetype
-- ----------------------------
INSERT INTO `feetype` VALUES ('1', '中药费');
INSERT INTO `feetype` VALUES ('2', '煎药费');
INSERT INTO `feetype` VALUES ('3', 'B超');
INSERT INTO `feetype` VALUES ('4', '检查');
INSERT INTO `feetype` VALUES ('5', '治疗');
INSERT INTO `feetype` VALUES ('7', '治未病');
INSERT INTO `feetype` VALUES ('8', '中成药');
INSERT INTO `feetype` VALUES ('9', '西药');
INSERT INTO `feetype` VALUES ('10', '膏方');
INSERT INTO `feetype` VALUES ('11', '挂号费');
INSERT INTO `feetype` VALUES ('12', '中草药');

-- ----------------------------
-- Table structure for insurancechargehistory
-- ----------------------------
DROP TABLE IF EXISTS `insurancechargehistory`;
CREATE TABLE `insurancechargehistory` (
  `zd1` varchar(10) DEFAULT NULL,
  `zd2` varchar(20) DEFAULT NULL,
  `zd3` varchar(18) DEFAULT NULL,
  `zd4` varchar(6) DEFAULT NULL,
  `zd5` varchar(50) DEFAULT NULL,
  `zd6` varchar(8) DEFAULT NULL,
  `zd7` int(4) DEFAULT NULL,
  `zd8` varchar(8) DEFAULT NULL,
  `zd9` varchar(10) DEFAULT NULL,
  `zd10` varchar(100) DEFAULT NULL,
  `zd11` varchar(10) DEFAULT NULL,
  `zd12` varchar(50) DEFAULT NULL,
  `zd13` varchar(50) DEFAULT NULL,
  `zd14` decimal(8,2) DEFAULT NULL,
  `zd15` varchar(18) DEFAULT NULL,
  `zd16` varchar(18) DEFAULT NULL,
  `zd17` varchar(50) DEFAULT NULL,
  `zd18` varchar(20) DEFAULT NULL,
  `zd19` int(3) DEFAULT NULL,
  `zd20` varchar(20) DEFAULT NULL,
  `zd21` varchar(8) DEFAULT NULL,
  `zd22` varchar(4) DEFAULT NULL,
  `zd23` varchar(8) DEFAULT NULL,
  `zd24` varchar(4) DEFAULT NULL,
  `zd25` varchar(50) DEFAULT NULL,
  `zd26` decimal(8,2) DEFAULT NULL,
  `zd27` decimal(8,2) DEFAULT NULL,
  `zd28` decimal(8,2) DEFAULT NULL,
  `zd29` decimal(8,2) DEFAULT NULL,
  `zd30` decimal(8,2) DEFAULT NULL,
  `zd31` decimal(16,2) DEFAULT NULL,
  `zd32` decimal(8,2) DEFAULT NULL,
  `zd33` decimal(8,2) DEFAULT NULL,
  `zd34` decimal(8,2) DEFAULT NULL,
  `zd35` decimal(8,2) DEFAULT NULL,
  `zd36` decimal(8,2) DEFAULT NULL,
  `zd37` decimal(8,2) DEFAULT NULL,
  `zd38` varchar(20) DEFAULT NULL,
  `zd39` varchar(8) DEFAULT NULL,
  `zd40` varchar(4) DEFAULT NULL,
  `zd41` decimal(8,2) DEFAULT NULL,
  `zd42` decimal(8,2) DEFAULT NULL,
  `zd43` decimal(8,2) DEFAULT NULL,
  `zd44` decimal(8,2) DEFAULT NULL,
  `zd45` decimal(8,2) DEFAULT NULL,
  `zd46` decimal(8,2) DEFAULT NULL,
  `zd47` decimal(16,2) DEFAULT NULL,
  `zd48` decimal(16,2) DEFAULT NULL,
  `zd49` decimal(16,2) DEFAULT NULL,
  `zd50` decimal(16,2) DEFAULT NULL,
  `zd51` decimal(16,2) DEFAULT NULL,
  `zd52` decimal(16,2) DEFAULT NULL,
  `zd53` decimal(16,2) DEFAULT NULL,
  `zd54` decimal(16,2) DEFAULT NULL,
  `zd55` decimal(16,2) DEFAULT NULL,
  `preid` int(11) DEFAULT NULL,
  `useful` tinyint(1) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of insurancechargehistory
-- ----------------------------

-- ----------------------------
-- Table structure for integrate
-- ----------------------------
DROP TABLE IF EXISTS `integrate`;
CREATE TABLE `integrate` (
  `level` int(11) NOT NULL,
  `integrate` int(11) NOT NULL,
  `discount` decimal(10,2) NOT NULL,
  PRIMARY KEY (`level`),
  UNIQUE KEY `level_UNIQUE` (`level`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of integrate
-- ----------------------------
INSERT INTO `integrate` VALUES ('1', '500', '0.80');
INSERT INTO `integrate` VALUES ('2', '0', '0.90');

-- ----------------------------
-- Table structure for medicine
-- ----------------------------
DROP TABLE IF EXISTS `medicine`;
CREATE TABLE `medicine` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `itemCode` varchar(20) DEFAULT NULL,
  `itemGrade` varchar(10) DEFAULT NULL,
  `name` varchar(45) NOT NULL,
  `name2` varchar(45) DEFAULT NULL,
  `unitId` int(11) NOT NULL,
  `inprice` decimal(10,2) NOT NULL DEFAULT '0.00',
  `outprice` decimal(10,2) NOT NULL,
  `feetypeId` int(11) NOT NULL,
  `factoryId` int(11) DEFAULT NULL,
  `stock` decimal(10,2) DEFAULT '0.00',
  `warn` decimal(10,2) DEFAULT NULL,
  `spell` varchar(45) NOT NULL,
  `type` varchar(45) NOT NULL DEFAULT '0',
  `isInclude` int(11) DEFAULT NULL,
  `useful` int(11) DEFAULT '1',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `name_UNIQUE` (`name`),
  KEY `unit_idx` (`unitId`),
  KEY `fatory_idx` (`factoryId`),
  KEY `feetype_idx` (`feetypeId`),
  CONSTRAINT `fatory` FOREIGN KEY (`factoryId`) REFERENCES `factory` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `feetype` FOREIGN KEY (`feetypeId`) REFERENCES `feetype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `unit` FOREIGN KEY (`unitId`) REFERENCES `unit` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=641 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of medicine
-- ----------------------------
INSERT INTO `medicine` VALUES ('1', '', '', '凑整费', 'test3', '1', '0.00', '0.01', '1', '1', '1000.00', '1000.00', 'CZF', '药品费', '0', '1');
INSERT INTO `medicine` VALUES ('618', 'Z-0008', '甲类', '艾炭', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'AT', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('619', 'Y-0018', '甲类', '巴戟天', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'BJT', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('620', 'Y-0029', '甲类', '白蔹', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'BL', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('621', 'Y-0039', '甲类', '白术', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'BS', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('622', 'Y-0049', '甲类', '百部', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'BB', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('623', 'Y-0059', '甲类', '半枝莲', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'BZL', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('624', 'Y-0070', '甲类', '薜荔', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'BL', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('625', 'Y-0080', '甲类', '补骨脂', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'BGZ', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('626', 'Y-0091', '甲类', '草河车', null, '1', '0.00', '1.00', '12', '1', '-6.00', '100.00', 'CHC', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('627', 'Y-0102', '甲类', '常山', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'CS', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('628', 'Y-0113', '乙类', '虫草花', null, '1', '0.00', '1.00', '12', '1', '-3.00', '100.00', 'CCH', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('629', 'Y-0133', '甲类', '葱花啊', '', '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'CHA', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('630', 'Y-0143', '甲类', '大青叶', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'DQY', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('631', 'Y-0155', '甲类', '淡竹叶', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'DZY', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('632', 'Y-0174', '甲类', '颠茄草', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'DQC', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('633', 'Y-0186', '甲类', '独一味', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'DYW', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('634', 'Y-0197', '甲类', '翻白草', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'FBC', '药品费', '1', '1');
INSERT INTO `medicine` VALUES ('635', 'Y-0209', '非医保', '蜂胶', null, '1', '0.00', '1.00', '12', '1', '0.00', '100.00', 'FJ', '药品费', '0', '0');
INSERT INTO `medicine` VALUES ('639', '123', '非医保', '爱的', '的', '1', '0.00', '2.00', '1', null, null, null, 'AD', '诊疗费', '0', '1');
INSERT INTO `medicine` VALUES ('640', '', '非医保', '啊啊啊', '', '2', '0.00', '5.00', '1', null, null, null, 'AAA', '诊疗费', '0', '1');

-- ----------------------------
-- Table structure for order
-- ----------------------------
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,
  `positionId` int(11) NOT NULL,
  `vipId` int(10) unsigned DEFAULT NULL,
  `card` decimal(10,2) DEFAULT '0.00',
  `given` decimal(10,2) DEFAULT '0.00',
  `cash` decimal(10,2) DEFAULT '0.00',
  `other` decimal(10,2) DEFAULT '0.00',
  `insurance` decimal(10,2) DEFAULT '0.00',
  `medicinediscount` decimal(8,4) NOT NULL DEFAULT '1.0000',
  `medcurediscount` decimal(8,4) NOT NULL DEFAULT '1.0000',
  `date` datetime DEFAULT NULL,
  `sum` decimal(10,2) NOT NULL DEFAULT '0.00',
  `insuranceNumber` varchar(18) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `ka_idx` (`card`),
  KEY `vip_idx` (`vipId`),
  KEY `ord_idx` (`positionId`),
  CONSTRAINT `ord` FOREIGN KEY (`positionId`) REFERENCES `position` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `vip` FOREIGN KEY (`vipId`) REFERENCES `vip` (`id`) ON DELETE NO ACTION ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of order
-- ----------------------------
INSERT INTO `order` VALUES ('1', '1', null, null, null, '15.00', '5.00', '0.00', '1.0000', '1.0000', '2018-12-24 15:48:55', '20.00', null);
INSERT INTO `order` VALUES ('3', '1', null, null, null, '0.00', '0.00', '10.00', '1.0000', '1.0000', '2018-12-25 15:19:37', '10.00', '0124116072');
INSERT INTO `order` VALUES ('5', '1', null, null, null, '0.00', '0.00', '16.00', '1.0000', '1.0000', '2018-12-25 15:26:45', '16.00', '0124118964');
INSERT INTO `order` VALUES ('6', '1', null, null, null, '300.00', '2017.00', '0.00', '0.8226', '0.8226', '2018-12-26 12:57:11', '2317.00', null);
INSERT INTO `order` VALUES ('7', '1', null, null, null, '52.00', '2291.00', '0.00', '0.9000', '0.7500', '2018-12-26 13:09:38', '2343.00', null);
INSERT INTO `order` VALUES ('8', '1', null, null, null, '852.00', '1965.00', '0.00', '1.0000', '1.0000', '2018-12-26 13:14:39', '2817.00', null);
INSERT INTO `order` VALUES ('9', '1', null, null, null, '500.00', '2010.00', '0.00', '0.8000', '1.0000', '2018-12-26 13:17:02', '2510.00', null);
INSERT INTO `order` VALUES ('10', '1', null, null, null, '600.00', '2217.00', '0.00', '1.0000', '1.0000', '2018-12-26 13:19:52', '2817.00', null);
INSERT INTO `order` VALUES ('11', '1', null, null, null, '500.00', '1717.00', '0.00', '0.7871', '0.7871', '2018-12-26 13:20:35', '2217.00', null);
INSERT INTO `order` VALUES ('12', '1', null, null, null, '800.00', '1180.00', '0.00', '0.7675', '0.7675', '2018-12-27 15:48:54', '1980.00', null);
INSERT INTO `order` VALUES ('13', '1', null, null, null, '55.00', '1962.00', '0.00', '0.7160', '0.7160', '2018-12-27 16:59:29', '2017.00', null);

-- ----------------------------
-- Table structure for patient
-- ----------------------------
DROP TABLE IF EXISTS `patient`;
CREATE TABLE `patient` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `sex` varchar(2) NOT NULL,
  `birth` date NOT NULL,
  `vip` int(6) unsigned zerofill DEFAULT NULL,
  `identity` varchar(45) DEFAULT NULL,
  `phone` varchar(45) DEFAULT NULL,
  `date` datetime NOT NULL,
  `last` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `vip_UNIQUE` (`vip`),
  UNIQUE KEY `phone_UNIQUE` (`phone`),
  UNIQUE KEY `identity_UNIQUE` (`identity`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of patient
-- ----------------------------
INSERT INTO `patient` VALUES ('8', '江加明', '男', '1998-03-25', '000001', '', '15570029212', '2018-12-26 12:40:36', '2018-12-27 16:59:07');
INSERT INTO `patient` VALUES ('9', '杨佳平', '男', '1998-03-21', null, null, null, '2018-12-27 15:46:59', '2018-12-27 15:46:59');

-- ----------------------------
-- Table structure for position
-- ----------------------------
DROP TABLE IF EXISTS `position`;
CREATE TABLE `position` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(45) NOT NULL,
  `number` varchar(45) NOT NULL,
  `password` varchar(45) NOT NULL,
  `power` varchar(45) NOT NULL DEFAULT '100000000000000',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  UNIQUE KEY `number_UNIQUE` (`number`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of position
-- ----------------------------
INSERT INTO `position` VALUES ('1', 'admin', 'admin', '123456', '111111111111111');
INSERT INTO `position` VALUES ('2', '导医台', '01', '123456', '110000000000000');
INSERT INTO `position` VALUES ('3', '药房', '02', '123456', '100011000000000');
INSERT INTO `position` VALUES ('4', '医生', '03', '123456', '101000000000000');
INSERT INTO `position` VALUES ('5', '收费处', '04', '123456', '100100000000000');

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prescription
-- ----------------------------
INSERT INTO `prescription` VALUES ('27', '639', '123', '非医保', '爱的', '的', '0.00', '2.00', '诊疗费', '克', null, '中药费', '639.70');
INSERT INTO `prescription` VALUES ('27', '630', 'Y-0143', '甲类', '大青叶', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '1533.00');
INSERT INTO `prescription` VALUES ('27', '640', '', '非医保', '啊啊啊', '', '0.00', '6.00', '诊疗费', '克', null, '煎药费', '0.70');
INSERT INTO `prescription` VALUES ('27', '0', 'Y-0229', '甲类', '甘草', '甘草', '0.00', '0.01', '药品费', '克', '凑整费', '凑整费', '10.00');
INSERT INTO `prescription` VALUES ('28', '639', '123', '非医保', '爱的', '的', '0.00', '2.00', '诊疗费', '克', null, '中药费', '639.70');
INSERT INTO `prescription` VALUES ('28', '630', 'Y-0143', '甲类', '大青叶', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '1533.00');
INSERT INTO `prescription` VALUES ('28', '640', '', '非医保', '啊啊啊', '', '0.00', '6.00', '诊疗费', '克', null, '煎药费', '0.70');
INSERT INTO `prescription` VALUES ('28', '0', 'Y-0229', '甲类', '甘草', '甘草', '0.00', '0.01', '药品费', '克', '凑整费', '凑整费', '67.00');
INSERT INTO `prescription` VALUES ('29', '639', '123', '非医保', '爱的', '的', '0.00', '2.00', '诊疗费', '克', null, '中药费', '639.70');
INSERT INTO `prescription` VALUES ('29', '630', 'Y-0143', '甲类', '大青叶', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '1533.00');
INSERT INTO `prescription` VALUES ('29', '640', '', '非医保', '啊啊啊', '', '0.00', '6.00', '诊疗费', '克', null, '煎药费', '0.70');
INSERT INTO `prescription` VALUES ('29', '0', 'Y-0229', '甲类', '甘草', '甘草', '0.00', '0.01', '药品费', '克', '凑整费', '凑整费', '40.00');
INSERT INTO `prescription` VALUES ('30', '639', '123', '非医保', '爱的', '的', '0.00', '2.00', '诊疗费', '克', null, '中药费', '639.70');
INSERT INTO `prescription` VALUES ('30', '630', 'Y-0143', '甲类', '大青叶', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '1533.00');
INSERT INTO `prescription` VALUES ('30', '640', '', '非医保', '啊啊啊', '', '0.00', '6.00', '诊疗费', '克', null, '煎药费', '0.70');
INSERT INTO `prescription` VALUES ('30', '0', 'Y-0229', '甲类', '甘草', '甘草', '0.00', '0.01', '药品费', '克', '凑整费', '凑整费', '0.00');
INSERT INTO `prescription` VALUES ('31', '639', '123', '非医保', '爱的', '的', '0.00', '2.00', '诊疗费', '克', null, '中药费', '639.70');
INSERT INTO `prescription` VALUES ('31', '630', 'Y-0143', '甲类', '大青叶', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '1533.00');
INSERT INTO `prescription` VALUES ('31', '640', '', '非医保', '啊啊啊', '', '0.00', '6.00', '诊疗费', '克', null, '煎药费', '0.70');
INSERT INTO `prescription` VALUES ('31', '0', 'Y-0229', '甲类', '甘草', '甘草', '0.00', '0.01', '药品费', '克', '凑整费', '凑整费', '40.00');
INSERT INTO `prescription` VALUES ('32', '639', '123', '非医保', '爱的', '的', '0.00', '2.00', '诊疗费', '克', null, '中药费', '639.70');
INSERT INTO `prescription` VALUES ('32', '630', 'Y-0143', '甲类', '大青叶', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '1533.00');
INSERT INTO `prescription` VALUES ('32', '640', '', '非医保', '啊啊啊', '', '0.00', '6.00', '诊疗费', '克', null, '煎药费', '0.70');
INSERT INTO `prescription` VALUES ('32', '0', 'Y-0229', '甲类', '甘草', '甘草', '0.00', '0.01', '药品费', '克', '凑整费', '凑整费', '8.00');
INSERT INTO `prescription` VALUES ('33', '618', 'Z-0008', '甲类', '艾炭', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '12.50');
INSERT INTO `prescription` VALUES ('33', '630', 'Y-0143', '甲类', '大青叶', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '12.68');
INSERT INTO `prescription` VALUES ('33', '633', 'Y-0186', '甲类', '独一味', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '1200.00');
INSERT INTO `prescription` VALUES ('33', '626', 'Y-0091', '甲类', '草河车', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '1354.00');
INSERT INTO `prescription` VALUES ('33', '0', 'Y-0229', '甲类', '甘草', '甘草', '0.00', '0.01', '药品费', '克', '凑整费', '凑整费', '63.00');
INSERT INTO `prescription` VALUES ('34', '639', '123', '非医保', '爱的', '的', '0.00', '2.00', '诊疗费', '克', null, '中药费', '639.70');
INSERT INTO `prescription` VALUES ('34', '630', 'Y-0143', '甲类', '大青叶', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '1533.00');
INSERT INTO `prescription` VALUES ('34', '640', '', '非医保', '啊啊啊', '', '0.00', '5.00', '诊疗费', '次', null, '中药费', '0.70');
INSERT INTO `prescription` VALUES ('34', '0', 'Y-0229', '甲类', '甘草', '甘草', '0.00', '0.01', '药品费', '克', '凑整费', '凑整费', '115.00');
INSERT INTO `prescription` VALUES ('35', '639', '123', '非医保', '爱的', '的', '0.00', '2.00', '诊疗费', '克', null, '中药费', '639.70');
INSERT INTO `prescription` VALUES ('35', '630', 'Y-0143', '甲类', '大青叶', null, '0.00', '1.00', '药品费', '克', '厚德医药有限公司', '中草药', '1533.00');
INSERT INTO `prescription` VALUES ('35', '640', '', '非医保', '啊啊啊', '', '0.00', '5.00', '诊疗费', '次', null, '中药费', '0.70');
INSERT INTO `prescription` VALUES ('35', '0', 'Y-0229', '甲类', '甘草', '甘草', '0.00', '0.01', '药品费', '克', '凑整费', '凑整费', '0.00');

-- ----------------------------
-- Table structure for prescriptionlist
-- ----------------------------
DROP TABLE IF EXISTS `prescriptionlist`;
CREATE TABLE `prescriptionlist` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rgid` int(11) DEFAULT NULL,
  `orderid` int(11) DEFAULT NULL,
  `dose` int(11) DEFAULT NULL,
  `medfee` decimal(10,2) DEFAULT NULL,
  `curefee` decimal(10,2) DEFAULT NULL,
  `state` int(1) DEFAULT NULL,
  `discription` varchar(512) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of prescriptionlist
-- ----------------------------
INSERT INTO `prescriptionlist` VALUES ('27', '69', '6', '1', '1533.10', '1283.60', '2', '');
INSERT INTO `prescriptionlist` VALUES ('28', '69', '7', '1', '1533.67', '1283.60', '2', '');
INSERT INTO `prescriptionlist` VALUES ('29', '69', '8', '1', '1533.40', '1283.60', '2', '');
INSERT INTO `prescriptionlist` VALUES ('30', '69', '9', '1', '1533.00', '1283.60', '2', '');
INSERT INTO `prescriptionlist` VALUES ('31', '69', '10', '1', '1533.40', '1283.60', '2', '');
INSERT INTO `prescriptionlist` VALUES ('32', '69', '11', '1', '1533.08', '1283.60', '2', '');
INSERT INTO `prescriptionlist` VALUES ('33', '70', '12', '2', '2579.81', '0.00', '2', '');
INSERT INTO `prescriptionlist` VALUES ('34', '71', '13', '1', '1534.15', '1282.90', '2', '');
INSERT INTO `prescriptionlist` VALUES ('35', '71', null, '1', '1533.00', '1282.90', '1', '');

-- ----------------------------
-- Table structure for register
-- ----------------------------
DROP TABLE IF EXISTS `register`;
CREATE TABLE `register` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `patientid` int(11) NOT NULL,
  `departmentId` int(11) NOT NULL,
  `doctorId` int(11) NOT NULL,
  `regtypeid` int(11) NOT NULL,
  `fee` decimal(12,2) NOT NULL,
  `first` varchar(45) NOT NULL DEFAULT '0',
  `positionId` int(11) NOT NULL,
  `date` datetime(6) NOT NULL,
  `useful` int(11) NOT NULL DEFAULT '1',
  `insurance` tinyint(1) DEFAULT '0',
  `securityId` varchar(10) DEFAULT NULL,
  `cardId` varchar(18) DEFAULT NULL,
  `areaId` varchar(6) DEFAULT NULL,
  `clinicNumber` varchar(45) DEFAULT NULL,
  `insuranceType` int(3) DEFAULT NULL,
  `entityCode` varchar(20) DEFAULT NULL,
  `entityName` varchar(100) DEFAULT NULL,
  `illness` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`),
  KEY `depart_idx` (`departmentId`),
  KEY `typeid_idx` (`regtypeid`),
  KEY `regi_idx` (`positionId`),
  KEY `pa_idx` (`patientid`),
  KEY `staff2_idx` (`doctorId`),
  CONSTRAINT `depart` FOREIGN KEY (`departmentId`) REFERENCES `department` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `pa` FOREIGN KEY (`patientid`) REFERENCES `patient` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `regi` FOREIGN KEY (`positionId`) REFERENCES `position` (`id`) ON DELETE NO ACTION ON UPDATE CASCADE,
  CONSTRAINT `regtypeid` FOREIGN KEY (`regtypeid`) REFERENCES `regtype` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `staff2` FOREIGN KEY (`doctorId`) REFERENCES `doctor` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=72 DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of register
-- ----------------------------
INSERT INTO `register` VALUES ('69', '8', '1', '7', '1', '0.00', '是', '1', '2018-12-26 12:40:35.902000', '1', null, null, null, null, null, null, null, null, '');
INSERT INTO `register` VALUES ('70', '9', '1', '7', '1', '6.00', '是', '1', '2018-12-27 15:46:58.935000', '1', null, null, null, null, null, null, null, null, '');
INSERT INTO `register` VALUES ('71', '8', '1', '7', '1', '0.00', '否', '1', '2018-12-27 16:59:06.957000', '1', null, null, null, null, null, null, null, null, '');

-- ----------------------------
-- Table structure for regtype
-- ----------------------------
DROP TABLE IF EXISTS `regtype`;
CREATE TABLE `regtype` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of regtype
-- ----------------------------
INSERT INTO `regtype` VALUES ('1', '免挂号费');
INSERT INTO `regtype` VALUES ('2', '预约挂号');

-- ----------------------------
-- Table structure for stockrecord
-- ----------------------------
DROP TABLE IF EXISTS `stockrecord`;
CREATE TABLE `stockrecord` (
  `id` int(11) NOT NULL,
  `medicineId` int(11) NOT NULL,
  `number` decimal(10,2) NOT NULL,
  `usefulDate` date DEFAULT NULL,
  `positionId` int(11) NOT NULL,
  `date` date NOT NULL,
  PRIMARY KEY (`id`,`medicineId`),
  KEY `sto_idx` (`positionId`),
  CONSTRAINT `sto` FOREIGN KEY (`positionId`) REFERENCES `position` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of stockrecord
-- ----------------------------

-- ----------------------------
-- Table structure for unit
-- ----------------------------
DROP TABLE IF EXISTS `unit`;
CREATE TABLE `unit` (
  `id` int(11) NOT NULL,
  `name` varchar(45) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of unit
-- ----------------------------
INSERT INTO `unit` VALUES ('1', '克');
INSERT INTO `unit` VALUES ('2', '次');
INSERT INTO `unit` VALUES ('3', '个');
INSERT INTO `unit` VALUES ('4', '只');
INSERT INTO `unit` VALUES ('5', '盒');
INSERT INTO `unit` VALUES ('6', '包');
INSERT INTO `unit` VALUES ('7', '罐');
INSERT INTO `unit` VALUES ('8', '袋');
INSERT INTO `unit` VALUES ('9', '根');
INSERT INTO `unit` VALUES ('10', '份');
INSERT INTO `unit` VALUES ('11', '支');
INSERT INTO `unit` VALUES ('12', '对');
INSERT INTO `unit` VALUES ('13', '副');
INSERT INTO `unit` VALUES ('14', '颗');
INSERT INTO `unit` VALUES ('15', '块');
INSERT INTO `unit` VALUES ('16', '粒');
INSERT INTO `unit` VALUES ('17', '30克');
INSERT INTO `unit` VALUES ('18', '40克');
INSERT INTO `unit` VALUES ('19', '60克');
INSERT INTO `unit` VALUES ('20', '条');

-- ----------------------------
-- Table structure for vip
-- ----------------------------
DROP TABLE IF EXISTS `vip`;
CREATE TABLE `vip` (
  `id` int(6) unsigned zerofill NOT NULL,
  `password` varchar(10) NOT NULL DEFAULT '123456',
  `consume` decimal(8,2) NOT NULL DEFAULT '0.00',
  `remain` decimal(8,2) NOT NULL DEFAULT '0.00',
  `present` decimal(8,2) NOT NULL DEFAULT '0.00',
  `useful` int(1) NOT NULL DEFAULT '2',
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------
-- Records of vip
-- ----------------------------
INSERT INTO `vip` VALUES ('000001', '123456', '5000.00', '5000.00', '500.00', '2');
INSERT INTO `vip` VALUES ('123456', '123456', '0.00', '0.00', '0.00', '2');

-- ----------------------------
-- View structure for all_register_view
-- ----------------------------
DROP VIEW IF EXISTS `all_register_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `all_register_view` AS select `register`.`id` AS `id`,`register`.`patientid` AS `patientid`,`patient`.`name` AS `patientname`,`patient`.`sex` AS `sex`,`patient`.`birth` AS `birth`,`patient`.`phone` AS `phone`,`patient`.`identity` AS `identity`,`department`.`name` AS `departname`,`doctor`.`name` AS `doctorname`,`regtype`.`name` AS `regtypename`,`register`.`fee` AS `fee`,`register`.`first` AS `first`,`position`.`name` AS `positionname`,`register`.`date` AS `date`,`register`.`useful` AS `useful`,`register`.`insurance` AS `insurance`,`register`.`securityId` AS `securityId`,`register`.`cardId` AS `cardId`,`register`.`areaId` AS `areaId`,`register`.`clinicNumber` AS `clinicNumber`,`register`.`insuranceType` AS `insuranceType`,`register`.`entityCode` AS `entityCode`,`register`.`entityName` AS `entityName`,`register`.`illness` AS `illness` from (((((`patient` join `doctor`) join `register`) join `department`) join `regtype`) join `position`) where ((`register`.`patientid` = `patient`.`id`) and (`regtype`.`id` = `register`.`regtypeid`) and (`register`.`departmentId` = `department`.`id`) and (`register`.`doctorId` = `doctor`.`id`) and (`register`.`positionId` = `position`.`id`)) ;

-- ----------------------------
-- View structure for chargeandrecharge_view
-- ----------------------------
DROP VIEW IF EXISTS `chargeandrecharge_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `chargeandrecharge_view` AS select `chargerecord`.`card` AS `card`,`vip_view`.`phone` AS `phone`,(`chargerecord`.`remain` + `chargerecord`.`present`) AS `sum`,`chargerecord`.`date` AS `date` from (`chargerecord` join `vip_view`) where (`chargerecord`.`card` = `vip_view`.`id`) union select `order`.`vipId` AS `card`,`vip_view`.`phone` AS `phone`,-((`order`.`card` + `order`.`given`)) AS `sum`,`order`.`date` AS `date` from (`order` join `vip_view`) where ((`order`.`vipId` = `vip_view`.`id`) and ((`order`.`card` <> 0) or (`order`.`given` <> 0))) ;

-- ----------------------------
-- View structure for count_depart_view
-- ----------------------------
DROP VIEW IF EXISTS `count_depart_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `count_depart_view` AS select `temp`.`name` AS `name`,`temp`.`income` AS `sumincome`,`temp`.`feetype` AS `feetype`,`temp`.`date` AS `date` from (select `re`.`id` AS `id`,`re`.`departname` AS `name`,`re`.`fee` AS `income`,'挂号费' AS `medname`,'挂号费' AS `feetype`,`re`.`date` AS `date` from `hospital2`.`all_register_view` `re` union select `po`.`pid` AS `id`,`po`.`departname` AS `name`,((`pre`.`outprice` * `pre`.`number`) * `po`.`medicinediscount`) AS `income`,`pre`.`name` AS `medname`,`pre`.`feetypename` AS `feetype`,`po`.`date` AS `date` from (`hospital2`.`prescription_view` `pre` join `hospital2`.`pre_order_view` `po`) where ((`po`.`pid` = `pre`.`prescriptionid`) and (`po`.`state` >= 2) and ((`po`.`card` = 0) or isnull(`po`.`card`)) and ((`po`.`given` = 0) or isnull(`po`.`card`)) and (`pre`.`type` = '药品费')) union select `po`.`pid` AS `id`,`po`.`departname` AS `name`,((`pre`.`outprice` * `pre`.`number`) * `po`.`medcurediscount`) AS `income`,`pre`.`name` AS `medname`,`pre`.`feetypename` AS `feetype`,`po`.`date` AS `date` from (`hospital2`.`prescription_view` `pre` join `hospital2`.`pre_order_view` `po`) where ((`po`.`pid` = `pre`.`prescriptionid`) and (`po`.`state` >= 2) and ((`po`.`card` = 0) or isnull(`po`.`card`)) and ((`po`.`given` = 0) or isnull(`po`.`card`)) and (`pre`.`type` = '诊疗费'))) `temp` ;

-- ----------------------------
-- View structure for count_doctor_view
-- ----------------------------
DROP VIEW IF EXISTS `count_doctor_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`%` SQL SECURITY DEFINER VIEW `count_doctor_view` AS select `temp`.`name` AS `name`,`temp`.`income` AS `sumincome`,`temp`.`feetype` AS `feetype`,`temp`.`date` AS `date` from (select `re`.`id` AS `id`,`re`.`doctorname` AS `name`,`re`.`fee` AS `income`,'挂号费' AS `medname`,'挂号费' AS `feetype`,`re`.`date` AS `date` from `hospital2`.`all_register_view` `re` union select `po`.`pid` AS `id`,`po`.`doctorname` AS `name`,((`pre`.`outprice` * `pre`.`number`) * `po`.`medicinediscount`) AS `income`,`pre`.`name` AS `medname`,`pre`.`feetypename` AS `feetype`,`po`.`date` AS `date` from (`hospital2`.`prescription_view` `pre` join `hospital2`.`pre_order_view` `po`) where ((`po`.`pid` = `pre`.`prescriptionid`) and (`po`.`state` >= 2) and ((`po`.`card` = 0) or isnull(`po`.`card`)) and ((`po`.`given` = 0) or isnull(`po`.`card`)) and (`pre`.`type` = '药品费')) union select `po`.`pid` AS `id`,`po`.`doctorname` AS `name`,((`pre`.`outprice` * `pre`.`number`) * `po`.`medcurediscount`) AS `income`,`pre`.`name` AS `medname`,`pre`.`feetypename` AS `feetype`,`po`.`date` AS `date` from (`hospital2`.`prescription_view` `pre` join `hospital2`.`pre_order_view` `po`) where ((`po`.`pid` = `pre`.`prescriptionid`) and (`po`.`state` >= 2) and ((`po`.`card` = 0) or isnull(`po`.`card`)) and ((`po`.`given` = 0) or isnull(`po`.`card`)) and (`pre`.`type` = '诊疗费'))) `temp` ;

-- ----------------------------
-- View structure for curesoldrecord_view
-- ----------------------------
DROP VIEW IF EXISTS `curesoldrecord_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `curesoldrecord_view` AS select `pre`.`name` AS `name`,`pre`.`unitname` AS `unitname`,`pre`.`inprice` AS `inprice`,`pre`.`outprice` AS `outprice`,`pre`.`number` AS `number`,round((`pre`.`inprice` * `pre`.`number`),3) AS `suminprice`,round((`pre`.`outprice` * `pre`.`number`),3) AS `sumoutprice`,round(((`pre`.`outprice` - `pre`.`inprice`) * `pre`.`number`),3) AS `income`,`po`.`date` AS `date` from (`prescription_view` `pre` join `pre_order_view` `po`) where ((`pre`.`prescriptionid` = `po`.`pid`) and (`po`.`state` >= 2) and (`pre`.`type` = '诊疗费')) ;

-- ----------------------------
-- View structure for doctor_view
-- ----------------------------
DROP VIEW IF EXISTS `doctor_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `doctor_view` AS select `doctor`.`id` AS `id`,`doctor`.`name` AS `drname`,`doctor`.`sex` AS `sex`,`doctor`.`birth` AS `birth`,`doctor`.`identity` AS `identity`,`doctor`.`phone` AS `phone`,`doctor`.`address` AS `address`,`department`.`name` AS `departname`,`doctor`.`useful` AS `useful` from (`doctor` join `department`) where (`doctor`.`departmentId` = `department`.`id`) ;

-- ----------------------------
-- View structure for getandlose_view
-- ----------------------------
DROP VIEW IF EXISTS `getandlose_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `getandlose_view` AS select `stockrecord`.`id` AS `id`,`medicine_view`.`name` AS `medname`,`medicine_view`.`unitname` AS `unitname`,`medicine_view`.`inprice` AS `inprice`,`stockrecord`.`number` AS `number`,(`medicine_view`.`inprice` * `stockrecord`.`number`) AS `profit`,`position`.`name` AS `posname`,`stockrecord`.`date` AS `date` from (((`stockrecord` join `countrecord`) join `position`) join `medicine_view`) where ((`stockrecord`.`id` = `countrecord`.`id`) and (`stockrecord`.`positionId` = `position`.`id`) and (`stockrecord`.`medicineId` = `medicine_view`.`id`)) ;

-- ----------------------------
-- View structure for medcure_view
-- ----------------------------
DROP VIEW IF EXISTS `medcure_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `medcure_view` AS select `medicine`.`id` AS `id`,`medicine`.`name` AS `name`,`medicine`.`name2` AS `name2`,`medicine`.`itemCode` AS `itemCode`,`medicine`.`itemGrade` AS `itemGrade`,`unit`.`name` AS `unitname`,ifnull(`medicine`.`inprice`,0) AS `inprice`,`medicine`.`outprice` AS `outprice`,ifnull(`medicine`.`stock`,0) AS `stock`,`feetype`.`name` AS `feetypename`,`medicine`.`spell` AS `spell`,`medicine`.`type` AS `type`,`medicine`.`useful` AS `useful`,`medicine`.`isInclude` AS `isInclude`,`factory`.`name` AS `factoryName` from (((`medicine` join `unit`) join `feetype`) left join `factory` on((`medicine`.`factoryId` = `factory`.`id`))) where ((`medicine`.`unitId` = `unit`.`id`) and (`medicine`.`feetypeId` = `feetype`.`id`)) ;

-- ----------------------------
-- View structure for medicine_view
-- ----------------------------
DROP VIEW IF EXISTS `medicine_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `medicine_view` AS select `medicine`.`id` AS `id`,`medicine`.`name` AS `name`,`unit`.`name` AS `unitname`,`medicine`.`name2` AS `name2`,`medicine`.`inprice` AS `inprice`,`medicine`.`outprice` AS `outprice`,`feetype`.`name` AS `feetypename`,`factory`.`name` AS `factoryname`,`medicine`.`stock` AS `stock`,`medicine`.`warn` AS `warn`,`medicine`.`spell` AS `spell`,`medicine`.`type` AS `type`,`medicine`.`useful` AS `useful`,`medicine`.`itemCode` AS `itemCode`,`medicine`.`isInclude` AS `isInclude`,`medicine`.`itemGrade` AS `itemGrade` from (((`medicine` join `unit`) join `feetype`) join `factory`) where ((`unit`.`id` = `medicine`.`unitId`) and (`medicine`.`feetypeId` = `feetype`.`id`) and (`medicine`.`factoryId` = `factory`.`id`)) ;

-- ----------------------------
-- View structure for medsoldrecord_view
-- ----------------------------
DROP VIEW IF EXISTS `medsoldrecord_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `medsoldrecord_view` AS select `pre`.`name` AS `name`,`pre`.`unitname` AS `unitname`,`pre`.`inprice` AS `inprice`,`pre`.`outprice` AS `outprice`,`pre`.`number` AS `number`,round((`pre`.`inprice` * `pre`.`number`),3) AS `suminprice`,round((`pre`.`outprice` * `pre`.`number`),3) AS `sumoutprice`,round(((`pre`.`outprice` - `pre`.`inprice`) * `pre`.`number`),3) AS `income`,`po`.`date` AS `date` from (`prescription_view` `pre` join `pre_order_view` `po`) where ((`pre`.`prescriptionid` = `po`.`pid`) and (`po`.`state` >= 2) and (`pre`.`type` = '药品费')) ;

-- ----------------------------
-- View structure for order_nodiscount_view
-- ----------------------------
DROP VIEW IF EXISTS `order_nodiscount_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `order_nodiscount_view` AS select `p`.`id` AS `pid`,`r`.`id` AS `rgid`,`r`.`patientname` AS `name`,`r`.`fee` AS `fee`,`r`.`departname` AS `departname`,`r`.`doctorname` AS `doctorname`,`p`.`medfee` AS `medfee`,`p`.`curefee` AS `curefee`,`o`.`id` AS `orderid`,`o`.`medicinediscount` AS `medicinediscount`,`o`.`medcurediscount` AS `medcurediscount`,`o`.`card` AS `card`,`o`.`given` AS `given`,`o`.`cash` AS `cash`,`o`.`other` AS `other`,`o`.`date` AS `date`,`p`.`state` AS `state` from ((`prescriptionlist` `p` left join `order` `o` on((`p`.`orderid` = `o`.`id`))) join `all_register_view` `r` on((`p`.`rgid` = `r`.`id`))) ;

-- ----------------------------
-- View structure for patient_view
-- ----------------------------
DROP VIEW IF EXISTS `patient_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `patient_view` AS select `a`.`id` AS `id`,`a`.`name` AS `name`,`a`.`sex` AS `sex`,`a`.`birth` AS `birth`,`a`.`vip` AS `vip`,`a`.`identity` AS `identity`,`a`.`phone` AS `phone`,`a`.`date` AS `date`,`a`.`last` AS `last`,ifnull(`c`.`sum`,0) AS `sum` from (((select `hospital2`.`patient`.`id` AS `id`,`hospital2`.`patient`.`name` AS `name`,`hospital2`.`patient`.`sex` AS `sex`,`hospital2`.`patient`.`birth` AS `birth`,`hospital2`.`patient`.`vip` AS `vip`,`hospital2`.`patient`.`identity` AS `identity`,`hospital2`.`patient`.`phone` AS `phone`,`hospital2`.`patient`.`date` AS `date`,`hospital2`.`patient`.`last` AS `last`,0 AS `sum` from `hospital2`.`patient`)) `a` left join (select `b`.`patientid` AS `id`,`b`.`name` AS `name`,`b`.`sex` AS `sex`,NULL AS `birth`,NULL AS `vip`,NULL AS `identity`,NULL AS `phone`,NULL AS `date`,NULL AS `last`,sum((((`b`.`card` + `b`.`given`) + `b`.`cash`) + `b`.`other`)) AS `sum` from `hospital2`.`pre_order_view` `b` where (`b`.`state` >= 2) group by `b`.`patientid`) `c` on((`c`.`id` = `a`.`id`))) ;

-- ----------------------------
-- View structure for prescription_t_view
-- ----------------------------
DROP VIEW IF EXISTS `prescription_t_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `prescription_t_view` AS select `p`.`listid` AS `prescriptionid`,`p`.`medicineid` AS `medid`,`p`.`medname` AS `name`,`medcure_view`.`unitname` AS `unitname`,`medcure_view`.`outprice` AS `outprice`,`medcure_view`.`inprice` AS `inprice`,`p`.`number` AS `number`,`medcure_view`.`feetypename` AS `feetypename`,`medcure_view`.`type` AS `type`,`medcure_view`.`itemCode` AS `itemCode`,`medcure_view`.`itemGrade` AS `itemGrade` from (`prescription` `p` join `medcure_view`) where (`p`.`medicineid` = `medcure_view`.`id`) ;

-- ----------------------------
-- View structure for prescription_view
-- ----------------------------
DROP VIEW IF EXISTS `prescription_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `prescription_view` AS select `p`.`listid` AS `prescriptionid`,`p`.`medicineid` AS `medid`,`p`.`medname` AS `name`,`p`.`medname2` AS `name2`,`p`.`unitName` AS `unitname`,`p`.`outprice` AS `outprice`,`p`.`inprice` AS `inprice`,`p`.`number` AS `number`,`p`.`feetypeName` AS `feetypename`,`p`.`type` AS `type`,`p`.`itemCode` AS `itemCode`,`p`.`itemGrade` AS `itemGrade` from `prescription` `p` ;

-- ----------------------------
-- View structure for pre_order_view
-- ----------------------------
DROP VIEW IF EXISTS `pre_order_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `pre_order_view` AS select `p`.`id` AS `pid`,`r`.`id` AS `rgid`,`r`.`patientname` AS `name`,`r`.`patientid` AS `patientid`,`r`.`sex` AS `sex`,timestampdiff(YEAR,`r`.`birth`,curdate()) AS `age`,`r`.`fee` AS `fee`,`r`.`departname` AS `departname`,`r`.`doctorname` AS `doctorname`,`r`.`date` AS `regdate`,`r`.`securityId` AS `securityId`,`r`.`cardId` AS `cardId`,`r`.`areaId` AS `areaId`,`r`.`clinicNumber` AS `clinicNumber`,`r`.`insuranceType` AS `insuranceType`,`r`.`entityCode` AS `entityCode`,`r`.`entityName` AS `entityName`,`r`.`illness` AS `illness`,`p`.`dose` AS `dose`,`p`.`medfee` AS `medfee`,`p`.`curefee` AS `curefee`,`p`.`discription` AS `discription`,`o`.`id` AS `orderid`,`o`.`medicinediscount` AS `medicinediscount`,`o`.`medcurediscount` AS `medcurediscount`,`o`.`card` AS `card`,`o`.`given` AS `given`,`o`.`cash` AS `cash`,`o`.`other` AS `other`,`o`.`sum` AS `sum`,`o`.`insurance` AS `insurance`,`o`.`date` AS `date`,`o`.`insuranceNumber` AS `insuranceNumber`,`p`.`state` AS `state` from (`all_register_view` `r` left join (`prescriptionlist` `p` left join `order` `o` on((`p`.`orderid` = `o`.`id`))) on((`p`.`rgid` = `r`.`id`))) ;

-- ----------------------------
-- View structure for profit_view
-- ----------------------------
DROP VIEW IF EXISTS `profit_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `profit_view` AS select `medicine_view`.`id` AS `id`,`medicine_view`.`name` AS `name`,`medicine_view`.`unitname` AS `unitname`,`medicine_view`.`inprice` AS `inprice`,`medicine_view`.`feetypename` AS `feetypename`,`medicine_view`.`factoryname` AS `factoryname`,`medicine_view`.`stock` AS `stock`,0 AS `truestock`,0 AS `number`,0 AS `profit` from `medicine_view` ;

-- ----------------------------
-- View structure for stockrecord_view
-- ----------------------------
DROP VIEW IF EXISTS `stockrecord_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `stockrecord_view` AS select `stockrecord`.`id` AS `id`,`stockrecord`.`medicineId` AS `medicineId`,`medicine_view`.`name` AS `medicinename`,`medicine_view`.`unitname` AS `unitname`,`medicine_view`.`inprice` AS `inprice`,`medicine_view`.`outprice` AS `outprice`,`medicine_view`.`feetypename` AS `feetypename`,`medicine_view`.`factoryname` AS `factoryname`,`medicine_view`.`stock` AS `stock`,`medicine_view`.`warn` AS `warn`,`stockrecord`.`number` AS `number`,`stockrecord`.`usefulDate` AS `usefuldate`,`position`.`name` AS `positionname`,`stockrecord`.`date` AS `date` from ((`stockrecord` join `medicine_view`) join `position`) where ((`stockrecord`.`medicineId` = `medicine_view`.`id`) and (`stockrecord`.`positionId` = `position`.`id`)) ;

-- ----------------------------
-- View structure for vip_view
-- ----------------------------
DROP VIEW IF EXISTS `vip_view`;
CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `vip_view` AS select `a`.`id` AS `patientId`,`a`.`name` AS `name`,`a`.`sex` AS `sex`,`a`.`birth` AS `birth`,`a`.`identity` AS `identity`,`a`.`phone` AS `phone`,`b`.`id` AS `id`,`b`.`password` AS `password`,`b`.`consume` AS `consume`,`b`.`remain` AS `remain`,`b`.`present` AS `present`,`b`.`useful` AS `useful` from (`vip` `b` join `patient` `a` on((`a`.`vip` = `b`.`id`))) ;

-- ----------------------------
-- Procedure structure for count_depart_procedure
-- ----------------------------
DROP PROCEDURE IF EXISTS `count_depart_procedure`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `count_depart_procedure`(IN start DATE, IN end DATE)
label_pro: BEGIN
SET @sql = NULL;
set @s = null;
set @e = null;
set @f = null;

set @s = concat(start,' 00:00:00');
set @e = concat(end,' 23:59:59');

select distinct(feetype) name into @f from count_depart_view where date between @s and @e limit 1;

if @f is null then 
leave label_pro;
end if;

SELECT 
    GROUP_CONCAT(CONCAT('SUM(IF(feetype = \'',
                t.name,
                '\',sumincome,0)) AS \'',
                t.name,
                '\''))
INTO @sql from (select distinct(feetype) name from count_depart_view where date between @s and @e) t;


SET @sql = CONCAT('SELECT name 名称, ', @sql, ' FROM count_depart_view WHERE date BETWEEN \'', start,' 00:00:00\' AND \'', end,' 23:59:59\' GROUP BY name');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;
END
;;
DELIMITER ;

-- ----------------------------
-- Procedure structure for count_doctor_procedure
-- ----------------------------
DROP PROCEDURE IF EXISTS `count_doctor_procedure`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `count_doctor_procedure`(IN start DATE, IN end DATE)
label_pro: BEGIN
SET @sql = NULL;
set @s = null;
set @e = null;
set @f = null;

set @s = concat(start,' 00:00:00');
set @e = concat(end,' 23:59:59');

select distinct(feetype) name into @f from count_doctor_view where date between @s and @e limit 1;

if @f is null then 
leave label_pro;
end if;

SELECT 
    GROUP_CONCAT(CONCAT('SUM(IF(feetype = \'',
                t.name,
                '\',sumincome,0)) AS \'',
                t.name,
                '\''))
INTO @sql FROM (select distinct(feetype) name from count_doctor_view where date between @s and @e) t;

SET @sql = CONCAT('SELECT name 名称, ', @sql, ' FROM count_doctor_view WHERE date BETWEEN \'', start,' 00:00:00\' AND \'', end,' 23:59:59\' GROUP BY name');
PREPARE statement FROM @sql;
EXECUTE statement;
DEALLOCATE PREPARE statement;
END
;;
DELIMITER ;

-- ----------------------------
-- Procedure structure for firsts_secone_visit
-- ----------------------------
DROP PROCEDURE IF EXISTS `firsts_secone_visit`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `firsts_secone_visit`(
in start datetime,
in last datetime
)
BEGIN
     (SELECT 
    a.patientid, a.patientname name,a.phone,ifnull(a.s,0) first, ifnull(b.s,0) second 
FROM
    (SELECT 
        patientid, patientname,phone,COUNT(first) s
    FROM
        all_register_view
    WHERE
        (first = '是')
            AND (date BETWEEN start AND last)
    GROUP BY patientid) a
        LEFT JOIN
    (SELECT 
        patientid, patientname,phone, COUNT(first) s
    FROM
        all_register_view
    WHERE
        (first = '否')
            AND (date BETWEEN start AND last)
    GROUP BY patientid) b ON (a.patientid = b.patientid)) UNION (SELECT 
    b.patientid, b.patientname name,b.phone,ifnull(a.s,0) first, ifnull(b.s,0) second
FROM
    (SELECT 
        patientid, patientname, phone,COUNT(first) s
    FROM
        all_register_view
    WHERE
        (first = '是')
            AND (date BETWEEN start AND last)
    GROUP BY patientid) a
        RIGHT JOIN
    (SELECT 
        patientid, patientname,phone, COUNT(first) s
    FROM
        all_register_view
    WHERE
        (first = '否')
            AND (date BETWEEN start AND last)
    GROUP BY patientid) b ON (a.patientid = b.patientid));
END
;;
DELIMITER ;

-- ----------------------------
-- Procedure structure for patient_procedure
-- ----------------------------
DROP PROCEDURE IF EXISTS `patient_procedure`;
DELIMITER ;;
CREATE DEFINER=`root`@`localhost` PROCEDURE `patient_procedure`(
in start datetime,
in last datetime
)
BEGIN

SELECT 
    `a`.`id` AS `id`,
    `a`.`name` AS `name`,
    `a`.`sex` AS `sex`,
    `a`.`birth` AS `birth`,
    `a`.`vip` AS `vip`,
    `a`.`identity` AS `identity`,
    `a`.`phone` AS `phone`,
    `a`.`date` AS `date`,
    `a`.`last` AS `last`,
    ifnull( `c`.`sum`,0) as sum
FROM
    (SELECT 
       `patient`.`id` AS `id`,
           `patient`.`name` AS `name`,
           `patient`.`sex` AS `sex`,
           `patient`.`birth` AS `birth`,
           `patient`.`vip` AS `vip`,
           `patient`.`identity` AS `identity`,
           `patient`.`phone` AS `phone`,
           `patient`.`date` AS `date`,
           `patient`.`last` AS `last`,
            0 AS `sum`
    FROM
       `patient`) `a`
        LEFT JOIN
    (SELECT 
        b.`patientid` AS `id`,
            b.`name` AS `name`,
            b.`sex` AS `sex`,
            NULL AS `birth`,
            NULL AS `vip`,
            NULL AS `identity`,
            NULL AS `phone`,
            NULL AS `date`,
            NULL AS `last`,
            SUM(ifnull(b.card,0) + ifnull(b.given ,0)+ ifnull(b.cash ,0) + ifnull(b.other,0)) AS `sum`
    FROM
        pre_order_view `b`
    WHERE
        b.state >= 2 and b.regdate between start and last
    GROUP BY b.patientid) c ON c.id = a.id
    order by sum desc
    
    ;

END
;;
DELIMITER ;
