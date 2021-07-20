/*
Navicat MySQL Data Transfer

Source Server         : 本地MySQL
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : lts

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2021-07-20 16:44:18
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for serverfile
-- ----------------------------
DROP TABLE IF EXISTS `serverfile`;
CREATE TABLE `serverfile` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `forName` varchar(50) NOT NULL COMMENT '消息发送者用户名',
  `toName` varchar(50) NOT NULL COMMENT '消息接收者用户名',
  `path` varchar(100) NOT NULL COMMENT '文件存储路径',
  `time` varchar(50) NOT NULL COMMENT '发送时间',
  `sign` int(4) NOT NULL COMMENT '标记是否已读（1已读 0未读）',
  `fileName` varchar(100) NOT NULL COMMENT '文件名',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
SET FOREIGN_KEY_CHECKS=1;
