package com.lh.idlestore.data.align.domain.mapper;

/**
 * 自动创建表
 */
public interface CreateTableMapper {

    /**
     * 创建日增量表：关注数计数变更
     */
    void createDataAlignFollowingCountTempTable(String tableNameSuffix);
}
