package com.learn.tcc;

import com.learn.entity.Storage;
import com.learn.mapper.StorageMapper;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StorageTccActionImpl implements StorageTccAction {
    @Autowired
    private StorageMapper storageMapper;

    @Transactional
    @Override
    public boolean prepare(BusinessActionContext ctx, Long productId, Integer count) {
        Storage s = storageMapper.selectByProductId(productId);
        if (s.getResidue() < count) {
            throw new RuntimeException("库存不足");
        }
        storageMapper.updateResidueToFrozen(productId, count); //有足够库存就冻结起来

        //幂等性控制
        //第一阶段成功时，设置成功标记 Action类--事务id--“flag"，解决幂等性问题
        ResultHolder.setResult(StorageTccAction.class,ctx.getXid(),"flag");
        return true;
    }

    @Transactional
    @Override
    public synchronized boolean commit(BusinessActionContext ctx) throws InterruptedException {
        //二阶段判断一阶段的标记是否存在，存在表示一阶段数据预存成功
        if(ResultHolder.getResult(StorageTccAction.class,ctx.getXid()) == null){
            return true;
        }

        Long productId = Long.valueOf(ctx.getActionContext("productId").toString());
        Integer count = Integer.valueOf(ctx.getActionContext("count").toString());
        storageMapper.updateFrozenToUsed(productId, count);

        //二阶段完成后，删除标记
        ResultHolder.removeResult(StorageTccAction.class,ctx.getXid());
        return true;
    }

    @Transactional
    @Override
    public synchronized boolean rollback(BusinessActionContext ctx) {
        /**
         一阶段失败，没有标记成功，回滚不执行
         */
        if(ResultHolder.getResult(StorageTccAction.class,ctx.getXid()) == null){
            return true;
        }

        Long productId = Long.valueOf(ctx.getActionContext("productId").toString());
        Integer count = Integer.valueOf(ctx.getActionContext("count").toString());
        storageMapper.updateFrozenToResidue(productId, count);

        //二阶段完成后，删除标记
        ResultHolder.removeResult(StorageTccAction.class, ctx.getXid());
        return true;
    }
}
