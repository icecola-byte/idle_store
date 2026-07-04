package com.lh.idlestore.distributed.id.generator.biz.service;

import com.alibaba.druid.pool.DruidDataSource;
import com.lh.idlestore.distributed.id.generator.biz.config.LeafProperties;
import com.lh.idlestore.distributed.id.generator.biz.core.IdGenerator;
import com.lh.idlestore.distributed.id.generator.biz.core.common.Result;
import com.lh.idlestore.distributed.id.generator.biz.core.common.ZeroIdGenerator;
import com.lh.idlestore.distributed.id.generator.biz.core.segment.SegmentIdGenerator;
import com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.IdAllocDao;
import com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.impl.IdAllocDaoImpl;
import com.lh.idlestore.distributed.id.generator.biz.exception.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
@Service("SegmentService")
public class SegmentService {
    private Logger logger = LoggerFactory.getLogger(SegmentService.class);

    private IdGenerator idGenerator;
    private DruidDataSource dataSource;

    public SegmentService(LeafProperties properties) throws SQLException, InitException {
        if (properties.getSegment().isEnable()) {
            // Config dataSource
            dataSource = new DruidDataSource();
            dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
            dataSource.setUrl(properties.getJdbc().getUrl());
            dataSource.setUsername(properties.getJdbc().getUsername());
            dataSource.setPassword(properties.getJdbc().getPassword());
            dataSource.setValidationQuery("select 1");
            dataSource.init();

            // Config Dao
            IdAllocDao dao = new IdAllocDaoImpl(dataSource);

            // Config ID Gen
            idGenerator = new SegmentIdGenerator();
            ((SegmentIdGenerator) idGenerator).setDao(dao);
            if (idGenerator.init()) {
                logger.info("Segment Service Init Successfully");
            } else {
                throw new InitException("Segment Service Init Fail");
            }
        } else {
            idGenerator = new ZeroIdGenerator();
            logger.info("Zero ID Gen Service Init Successfully");
        }
    }

    public Result getId(String key) {
        return idGenerator.get(key);
    }

    public SegmentIdGenerator getIdGenerator() {
        if (idGenerator instanceof SegmentIdGenerator) {
            return (SegmentIdGenerator) idGenerator;
        }
        return null;
    }
}
