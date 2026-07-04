package com.lh.idlestore.distributed.id.generator.biz.core;

import com.lh.idlestore.distributed.id.generator.biz.core.common.Result;

public interface IdGenerator {
    Result get(String key);
    boolean init();
}
