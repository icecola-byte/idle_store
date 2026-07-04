package com.lh.idlestore.distributed.id.generator.constant;

public final class DistributedIdGeneratorApiConstants {

    public static final String SERVICE_NAME =
            "idle-store-distributed-id-generator";

    public static final String API_PREFIX = "/id";

    public static final String OPERATION_GET_SEGMENT_ID =
            "getSegmentId";

    public static final String OPERATION_GET_SNOWFLAKE_ID =
            "getSnowflakeId";

    private DistributedIdGeneratorApiConstants() {
    }
}
