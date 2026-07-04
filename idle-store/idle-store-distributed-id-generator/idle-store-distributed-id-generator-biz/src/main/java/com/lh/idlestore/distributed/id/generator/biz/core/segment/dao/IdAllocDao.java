package com.lh.idlestore.distributed.id.generator.biz.core.segment.dao;


import com.lh.idlestore.distributed.id.generator.biz.core.segment.model.LeafAlloc;

import java.util.List;

public interface IdAllocDao {
     List<LeafAlloc> getAllLeafAllocs();
     LeafAlloc updateMaxIdAndGetLeafAlloc(String tag);
     LeafAlloc updateMaxIdByCustomStepAndGetLeafAlloc(LeafAlloc leafAlloc);
     List<String> getAllTags();
}
