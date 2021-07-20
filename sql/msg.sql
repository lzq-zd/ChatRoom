/*
Navicat MySQL Data Transfer

Source Server         : 本地MySQL
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : lts

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2021-07-20 16:44:07
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for msg
-- ----------------------------
DROP TABLE IF EXISTS `msg`;
CREATE TABLE `msg` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `forName` varchar(50) NOT NULL COMMENT '消息发送者用户名',
  `toName` varchar(50) NOT NULL COMMENT '消息接收者用户名',
  `msg` varchar(255) NOT NULL COMMENT '消息体',
  `datetime` datetime NOT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '消息发送时间',
  `sign` int(4) NOT NULL COMMENT '消息是否已读',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8;
SET FOREIGN_KEY_CHECKS=1;
