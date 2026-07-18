package com.lh.idlestore.data.align.job;

import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class CreateTableXxlJob {

    @XxlJob("createTableJobHandler")
    public void createTableJobHandler() throws Exception {
        XxlJobHelper.log("# 初始化明日增量数据表...");
    }

}
