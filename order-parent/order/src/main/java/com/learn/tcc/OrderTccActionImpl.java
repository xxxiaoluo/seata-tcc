package com.learn.tcc;

import com.learn.entity.Order;
import com.learn.mapper.OrderMapper;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * tcc失败问题：
 *      1.第一阶段冻结失败，二阶段回滚也会执行
 *      2.二阶段失败，TC重复发送二阶段提交或回滚指令，模块会重复执行二阶段操作
 */
@Component
public class OrderTccActionImpl implements OrderTccAction {
    @Autowired
    private OrderMapper orderMapper;

    @Transactional
    @Override
    public boolean prepare(BusinessActionContext ctx, Long id, Long userId, Long productId, Integer count, BigDecimal money) {
        orderMapper.createFrozen(new Order(id,userId,productId,count,money,0));

        //幂等性控制
        //第一阶段成功时，设置成功标记 Action类--事务id--“flag"，解决幂等性问题
        ResultHolder.setResult(OrderTccAction.class,ctx.getXid(),"flag");

        //true 预留数据成功
        //false 预留数据失败
        return true;
    }

    @Transactional
    @Override
    public synchronized boolean commit(BusinessActionContext ctx) {
        //二阶段判断一阶段的标记是否存在，存在表示一阶段数据预存成功
        if(ResultHolder.getResult(OrderTccAction.class,ctx.getXid()) == null){
            return true;
        }
        Long orderId = Long.valueOf(ctx.getActionContext("orderId").toString());
        orderMapper.updateStatus(orderId,1);
        //二阶段完成后，删除标记
        ResultHolder.removeResult(OrderTccAction.class, ctx.getXid());
        return true;
    }

    @Transactional
    @Override
    public synchronized boolean rollback(BusinessActionContext ctx) {
        /**
         一阶段失败，没有标记成功，回滚不执行
         一阶段有些模块成功有些失败，有标记，但要回滚
         */
        if(ResultHolder.getResult(OrderTccAction.class,ctx.getXid()) == null){
            return true;
        }
        Long orderId = Long.valueOf(ctx.getActionContext("orderId").toString());
        orderMapper.deleteById(orderId);

        //二阶段完成后，删除标记
        ResultHolder.removeResult(OrderTccAction.class, ctx.getXid());
        return true;
    }
}
