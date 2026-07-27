package com.lh.idlestore.commodity.mq.constant;

public final class CommodityMqConstants {

    private CommodityMqConstants() {
    }

    /**
     * 商品服务事件 Topic。
     */
    public static final String OUTBOX_TOPIC = "IDLE_STORE_COMMODITY_EVENT";

    /**
     * 商品分类缓存失效 Tag。
     */
    public static final String CATEGORY_CACHE_INVALIDATED_TAG =
            "CATEGORY_CACHE_INVALIDATED";

    /**
     * 过时图片清理 TAG
     */
    public static final String CATEGORY_ICON_FILE_DELETE_REQUEST_TAG =
            "CATEGORY_ICON_FILE_DELETE_REQUEST";

    /**
     * 分类缓存失效广播消费者组。
     */
    public static final String CATEGORY_CACHE_CONSUMER_GROUP =
            "idle-store-commodity-category-cache";

    /**
     * 图片清理消费者组
     */
    public static final String ICON_FILE_DELETE_GROUP =
            "idle-store-commodity-category-icon-file-delete";

    public static final int SEND_TIMEOUT_MILLIS = 3000;

    /**
     * 单次定时任务最多处理的事件数。
     */
    public static final int PUBLISH_BATCH_SIZE = 50;
}
