-- ============================================================================
-- idle-store-oss MySQL 初始化脚本
--
-- 文件位置由 storage_provider、bucket_name 和 object_key 唯一确定。
-- 业务服务仅保存 file_id，不建立跨服务数据库外键。
-- ============================================================================

DROP TABLE IF EXISTS `t_file_object`;
CREATE TABLE `t_file_object` (
  `file_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `storage_provider` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '存储提供方，例如 MINIO、ALIYUN_OSS',
  `bucket_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '对象存储 Bucket 名称',
  `object_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '对象存储 Key',
  `original_filename` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '原始文件名',
  `content_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '文件 MIME 类型',
  `file_size` bigint unsigned DEFAULT NULL COMMENT '文件大小，单位字节',
  `file_status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '文件状态：0正常，1删除中，2已删除',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `delete_time` datetime DEFAULT NULL COMMENT '物理删除成功时间',
  PRIMARY KEY (`file_id`),
  UNIQUE KEY `uk_storage_object` (`storage_provider`,`bucket_name`,`object_key`),
  KEY `idx_file_status_create_time` (`file_status`,`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='OSS 文件元数据表';

-- 历史商品图片均来自 typora-picture-0 阿里云北京 Bucket。
-- 文件仍保留在该 Bucket 时，读取会按 storage_provider=ALIYUN_OSS 路由到阿里云策略。
BEGIN;
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (1, 'ALIYUN_OSS', 'typora-picture-0', '4f7564d8c9194a6f953b7d8fa418f182.jpg', '4f7564d8c9194a6f953b7d8fa418f182.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (2, 'ALIYUN_OSS', 'typora-picture-0', '89a58157ed7141b79eb005c83889f945.webp', '89a58157ed7141b79eb005c83889f945.webp', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (3, 'ALIYUN_OSS', 'typora-picture-0', '225aad3b3d134fdeabb8b4836357cdf7.webp', '225aad3b3d134fdeabb8b4836357cdf7.webp', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (4, 'ALIYUN_OSS', 'typora-picture-0', '50021de427dc498bbae734a8daebcdeb.jpg', '50021de427dc498bbae734a8daebcdeb.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (5, 'ALIYUN_OSS', 'typora-picture-0', '3b4756e3413d4f59837cd5d9c5d3bd73.jpg', '3b4756e3413d4f59837cd5d9c5d3bd73.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (6, 'ALIYUN_OSS', 'typora-picture-0', '54e7a0d5e8224b668e33af7afdbc90d0.webp', '54e7a0d5e8224b668e33af7afdbc90d0.webp', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (7, 'ALIYUN_OSS', 'typora-picture-0', 'aa1a00a5c52f42618e1f4b316041fa21.jpg', 'aa1a00a5c52f42618e1f4b316041fa21.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (8, 'ALIYUN_OSS', 'typora-picture-0', '232829d5797c4530ac196b15974dc46e.webp', '232829d5797c4530ac196b15974dc46e.webp', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (9, 'ALIYUN_OSS', 'typora-picture-0', '3e02e818b2b349019313398b8105a5a7.webp', '3e02e818b2b349019313398b8105a5a7.webp', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (10, 'ALIYUN_OSS', 'typora-picture-0', 'da82ab5e67f945a7b24db867942631bd.webp', 'da82ab5e67f945a7b24db867942631bd.webp', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (11, 'ALIYUN_OSS', 'typora-picture-0', 'ec363cc67884432abee5bca14de6189e.webp', 'ec363cc67884432abee5bca14de6189e.webp', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (12, 'ALIYUN_OSS', 'typora-picture-0', '4d42b5a63f62453490f81e7233bbe2c5.webp', '4d42b5a63f62453490f81e7233bbe2c5.webp', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (13, 'ALIYUN_OSS', 'typora-picture-0', 'b35a8f077c4f4a07b77a742ac95647df.webp', 'b35a8f077c4f4a07b77a742ac95647df.webp', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (14, 'ALIYUN_OSS', 'typora-picture-0', 'a51dfcf281c544969e22c510e2bbe6ea.jpg', 'a51dfcf281c544969e22c510e2bbe6ea.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (15, 'ALIYUN_OSS', 'typora-picture-0', '3b5e06f73ed04294ba6cfd19003b3703.jpg', '3b5e06f73ed04294ba6cfd19003b3703.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (16, 'ALIYUN_OSS', 'typora-picture-0', 'c8e589cfae494c819852f9689d4a84b4.jpg', 'c8e589cfae494c819852f9689d4a84b4.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (17, 'ALIYUN_OSS', 'typora-picture-0', '898b7a219b5f4569b6ca52ee7991ed0a.jpg', '898b7a219b5f4569b6ca52ee7991ed0a.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (18, 'ALIYUN_OSS', 'typora-picture-0', '33f022a5f9e740d7860c6aba92a5fce2.jpg', '33f022a5f9e740d7860c6aba92a5fce2.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (19, 'ALIYUN_OSS', 'typora-picture-0', 'ce84652bdee94baea079a7b468944a04.jpg', 'ce84652bdee94baea079a7b468944a04.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (20, 'ALIYUN_OSS', 'typora-picture-0', 'a5c78aee7b8b44c8ac50a9e86ad1f8bc.jpg', 'a5c78aee7b8b44c8ac50a9e86ad1f8bc.jpg', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (21, 'ALIYUN_OSS', 'typora-picture-0', '064e9654dfec424db47c0cb8cc8ed34e.webp', '064e9654dfec424db47c0cb8cc8ed34e.webp', NULL, NULL, 0);
INSERT INTO `t_file_object` (`file_id`, `storage_provider`, `bucket_name`, `object_key`, `original_filename`, `content_type`, `file_size`, `file_status`) VALUES (22, 'MINIO', 'idlestore', '006ea1977d934ff0a5a88320eda4d04d.jpg', '006ea1977d934ff0a5a88320eda4d04d.jpg', NULL, NULL, 0);
COMMIT;
