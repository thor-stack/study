CREATE TABLE `tb_user` (
  `id` int NOT NULL,
  `name` varchar(32) NOT NULL,
  `sex` varchar(1) DEFAULT NULL,
  `address` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

insert into tb_user(id, name, sex, address) values(1, '张三', '男', '北京');
insert into tb_user(id, name, sex, address) values(2, '李四', '男', '上海');
insert into tb_user(id, name, sex, address) values(3, 'Alice', '女', '深圳');