package com.lh.idlestore.commodity.repository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lh.idlestore.commodity.repository.dataobject.CommodityOutboxDO;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品服务 Outbox 事件持久化 Mapper。
 */
public interface CommodityOutboxMapper extends BaseMapper<CommodityOutboxDO> {

    /**
     * 查询并锁定可投递事件。
     * 调用这个方法时必须已开启 MySQL 事务。
     */
    List<CommodityOutboxDO> selectReadyForUpdate(
            @Param("now") LocalDateTime now,
            @Param("limit") int limit
    );

    /**
     * RocketMQ 成功接收后标记事件已发送。
     */
    int markSent(
            @Param("eventId") Long eventId,
            @Param("sentTime") LocalDateTime sentTime
    );

    /**
     * RocketMQ 投递失败后，设置下一次重试时间。
     */
    int markRetry(
            @Param("eventId") Long eventId,
            @Param("nextRetryTime") LocalDateTime nextRetryTime
    );
}
