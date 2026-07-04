package com.lh.idlestore.distributed.id.generator.biz.service;

import com.lh.idlestore.distributed.id.generator.biz.config.LeafProperties;
import com.lh.idlestore.distributed.id.generator.biz.core.IdGenerator;
import com.lh.idlestore.distributed.id.generator.biz.core.common.ZeroIdGenerator;
import com.lh.idlestore.distributed.id.generator.biz.core.snowflake.SnowflakeIdGenerator;
import com.lh.idlestore.distributed.id.generator.biz.exception.InitException;
import com.lh.idlestore.distributed.id.generator.biz.core.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("SnowflakeService")
public class SnowflakeService {
    private Logger logger = LoggerFactory.getLogger(SnowflakeService.class);

    private IdGenerator idGenerator;

    public SnowflakeService(LeafProperties properties) throws InitException {
        if (properties.getSnowflake().isEnable()) {
            String zkAddress = properties.getSnowflake().getZk().getAddress();
            int port = properties.getSnowflake().getPort();
            idGenerator = new SnowflakeIdGenerator(zkAddress, port, properties.getName());
            if(idGenerator.init()) {
                logger.info("Snowflake Service Init Successfully");
            } else {
                throw new InitException("Snowflake Service Init Fail");
            }
        } else {
            idGenerator = new ZeroIdGenerator();
            logger.info("Zero ID Gen Service Init Successfully");
        }
    }

    public Result getId(String key) {
        return idGenerator.get(key);
    }
}
