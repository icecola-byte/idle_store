-- ============================================================================
-- idle-store-user MySQL 初始化脚本
--
-- 用户头像只保存 OSS 文件 ID。文件位置由 OSS 服务的 t_file_object 维护。
-- 该服务数据库不与 OSS 服务数据库建立物理外键。
-- ============================================================================

DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
  `user_id` bigint unsigned NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '手机号',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '密码哈希，管理员密码登录使用；验证码登录用户可为空',
  `avatar_file_id` bigint unsigned DEFAULT NULL COMMENT '头像文件ID，关联 OSS 服务文件元数据',
  `community_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户所属社区ID',
  `register_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `coin_balance` int NOT NULL DEFAULT '0' COMMENT '硬币余额',
  `status` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '状态：0正常，1禁用',
  `sex` tinyint unsigned DEFAULT '1' COMMENT '性别：1男，2女',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '真实姓名',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用户名/昵称',
  `address_detail` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '详细地址',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_deleted` tinyint unsigned NOT NULL DEFAULT '0' COMMENT '逻辑删除：0未删除，1已删除',
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE KEY `uk_user_phone` (`phone`) USING BTREE,
  KEY `idx_user_community_id` (`community_id`) USING BTREE,
  KEY `idx_user_status` (`status`) USING BTREE,
  KEY `idx_user_avatar_file_id` (`avatar_file_id`) USING BTREE
) ENGINE=InnoDB AUTO_INCREMENT=100000055 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci ROW_FORMAT=DYNAMIC COMMENT='用户表';

-- ---------------------------------------------------------------------------
-- 种子数据
--
-- file_id = 22 对应 OSS 服务中 MINIO / idlestore Bucket 的
-- 006ea1977d934ff0a5a88320eda4d04d.jpg 对象。
-- ---------------------------------------------------------------------------

BEGIN;
INSERT INTO `t_user` (`user_id`, `phone`, `password_hash`, `avatar_file_id`, `community_id`, `register_time`, `coin_balance`, `status`, `sex`, `real_name`, `username`, `address_detail`, `create_time`, `update_time`, `is_deleted`) VALUES (100000050, '15020248960', '$2a$10$oBJu3kHayic5qUefnvRD0eWepZ75mKdH0SHjpujEjxUNscBKmBfkC', 22, '371721003001', '2024-05-22 14:08:27', 490, 0, 1, '刘虎', '寂寞的冰可乐', '2号楼2单元202室', '2024-05-22 14:08:27', '2026-07-24 10:44:00', 0);
INSERT INTO `t_user` (`user_id`, `phone`, `password_hash`, `avatar_file_id`, `community_id`, `register_time`, `coin_balance`, `status`, `sex`, `real_name`, `username`, `address_detail`, `create_time`, `update_time`, `is_deleted`) VALUES (100000051, '15550169258', '$2a$10$oBJu3kHayic5qUefnvRD0eWepZ75mKdH0SHjpujEjxUNscBKmBfkC', 22, '371721003001', '2024-05-22 14:23:50', 0, 0, 1, '', '孤独的羊肉串', NULL, '2024-05-22 14:23:50', '2026-07-26 15:44:45', 0);
INSERT INTO `t_user` (`user_id`, `phone`, `password_hash`, `avatar_file_id`, `community_id`, `register_time`, `coin_balance`, `status`, `sex`, `real_name`, `username`, `address_detail`, `create_time`, `update_time`, `is_deleted`) VALUES (100000052, '15615509991', '$2a$10$oBJu3kHayic5qUefnvRD0eWepZ75mKdH0SHjpujEjxUNscBKmBfkC', NULL, '371721003001', '2026-07-07 09:47:46', 0, 0, 1, NULL, '15615509991', NULL, '2026-07-07 09:47:46', '2026-07-08 16:41:16', 0);
INSERT INTO `t_user` (`user_id`, `phone`, `password_hash`, `avatar_file_id`, `community_id`, `register_time`, `coin_balance`, `status`, `sex`, `real_name`, `username`, `address_detail`, `create_time`, `update_time`, `is_deleted`) VALUES (100000053, '17668664010', '$2a$10$oBJu3kHayic5qUefnvRD0eWepZ75mKdH0SHjpujEjxUNscBKmBfkC', NULL, '370103018001', '2026-07-07 09:47:46', 0, 0, 1, NULL, '17668664010', NULL, '2026-07-07 09:47:46', '2026-07-08 16:41:19', 0);
INSERT INTO `t_user` (`user_id`, `phone`, `password_hash`, `avatar_file_id`, `community_id`, `register_time`, `coin_balance`, `status`, `sex`, `real_name`, `username`, `address_detail`, `create_time`, `update_time`, `is_deleted`) VALUES (100000054, '13561336192', '$2a$10$oBJu3kHayic5qUefnvRD0eWepZ75mKdH0SHjpujEjxUNscBKmBfkC', NULL, '371721003002', '2026-07-07 09:47:46', 0, 0, 1, NULL, '13561336192', NULL, '2026-07-07 09:47:46', '2026-07-08 16:41:21', 0);
COMMIT;
