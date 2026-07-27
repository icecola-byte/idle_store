-- 执行前请确认索引不存在。MySQL 不同小版本对 IF NOT EXISTS 的支持并不一致。

-- 分类树递归查询子节点、删除分类树时使用。
ALTER TABLE t_commodity_category
    ADD INDEX idx_commodity_category_parent_deleted (parent_id, is_deleted);

-- 删除分类树前按分类 ID 查询未删除商品时使用。
ALTER TABLE t_commodity
    ADD INDEX idx_commodity_category_deleted (category_id, is_deleted);
