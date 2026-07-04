package com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.impl;

import com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.IdAllocDao;
import com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.IdAllocMapper;
import com.lh.idlestore.distributed.id.generator.biz.core.segment.model.LeafAlloc;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.TransactionFactory;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

import javax.sql.DataSource;
import java.util.List;

public class IdAllocDaoImpl implements IdAllocDao {

    SqlSessionFactory sqlSessionFactory;

    public IdAllocDaoImpl(DataSource dataSource) {
        TransactionFactory transactionFactory = new JdbcTransactionFactory();
        Environment environment = new Environment("development", transactionFactory, dataSource);
        Configuration configuration = new Configuration(environment);
        configuration.addMapper(IdAllocMapper.class);
        sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
    }

    @Override
    public List<LeafAlloc> getAllLeafAllocs() {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            return sqlSession.selectList("com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.IdAllocMapper.getAllLeafAllocs");
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public LeafAlloc updateMaxIdAndGetLeafAlloc(String tag) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            sqlSession.update("com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.IdAllocMapper.updateMaxId", tag);
            LeafAlloc result = sqlSession.selectOne("com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.IdAllocMapper.getLeafAlloc", tag);
            sqlSession.commit();
            return result;
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc) {
        SqlSession sqlSession = sqlSessionFactory.openSession();
        try {
            sqlSession.update("com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.IdAllocMapper.updateMaxIdByCustomStep", leafAlloc);
            LeafAlloc result = sqlSession.selectOne("com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.IdAllocMapper.getLeafAlloc", leafAlloc.getKey());
            sqlSession.commit();
            return result;
        } finally {
            sqlSession.close();
        }
    }

    @Override
    public List<String> getAllTags() {
        SqlSession sqlSession = sqlSessionFactory.openSession(false);
        try {
            return sqlSession.selectList("com.lh.idlestore.distributed.id.generator.biz.core.segment.dao.IdAllocMapper.getAllTags");
        } finally {
            sqlSession.close();
        }
    }
}
