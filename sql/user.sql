/*
Navicat MySQL Data Transfer

Source Server         : 本地MySQL
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : lts

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2021-07-20 16:44:24
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `Uid` int(10) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
  `Uname` varchar(50) NOT NULL COMMENT '用户名',
  `Upasswd` varchar(50) NOT NULL COMMENT '用户密码',
  `Uemail` varchar(50) NOT NULL COMMENT '用户邮箱',
  PRIMARY KEY (`Uid`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;
SET FOREIGN_KEY_CHECKS=1;
