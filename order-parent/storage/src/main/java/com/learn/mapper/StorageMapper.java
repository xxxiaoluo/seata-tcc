package com.learn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.entity.Storage;

public interface StorageMapper extends BaseMapper<Storage> {
    //减少库存
    void decrease(Long productId,Integer count);
    // 查询库存，用来判断是否有足够库存
    Storage selectByProductId(Long productId);
    // 可用 --> 冻结
    void updateResidueToFrozen(Long productId, Integer count);
    // 冻结 --> 已使用
    void updateFrozenToUsed(Long productId, Integer count);
    // 冻结 --> 可用
    void updateFrozenToResidue(Long productId, Integer count);
}
