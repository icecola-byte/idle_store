package com.lh.idlestore.distributed.id.generator.biz.core.common;


import com.lh.idlestore.distributed.id.generator.biz.core.IdGenerator;

public class ZeroIdGenerator implements IdGenerator {
    @Override
    public Result get(String key) {
        return new Result(0, Status.SUCCESS);
    }

    @Override
    public boolean init() {
        return true;
    }
}
