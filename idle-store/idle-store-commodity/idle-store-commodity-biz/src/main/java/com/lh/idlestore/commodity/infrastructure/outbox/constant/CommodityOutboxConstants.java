package com.lh.idlestore.commodity.infrastructure.outbox.constant;

public final class CommodityOutboxConstants {
    private CommodityOutboxConstants() {}

    public static final String OUTBOX_EVENT_ID_KEY = "commodity_outbox_event";

    public static final long MAX_RETRY_DELAY_SECONDS = 300L;

    public static final int MAX_RETRY_EXPONENT = 9;
}
