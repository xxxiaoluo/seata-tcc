package com.learn.tcc;

import io.seata.rm.tcc.api.BusinessActionContext;
import io.seata.rm.tcc.api.BusinessActionContextParameter;
import io.seata.rm.tcc.api.LocalTCC;
import io.seata.rm.tcc.api.TwoPhaseBusinessAction;

import java.math.BigDecimal;

@LocalTCC
public interface OrderTccAction {
    @TwoPhaseBusinessAction(name = "orderTccAction",
            commitMethod = "commit",
            rollbackMethod = "rollback")  //加了这个注解就是第一阶段方法，里面指定谁是二阶段提交方法和回滚方法.这里commit、rollback是默认值就是灰色(默认值可以不加)
    boolean prepare(BusinessActionContext ctx,
                    @BusinessActionContextParameter(paramName = "orderId") Long id,
                    Long userId,
                    Long productId,
                    Integer count,
                    BigDecimal money);  //BusinessActionContext用于一到二阶段的数据传输. @BusinessActionContextParameter将数据放入上下文

    boolean commit(BusinessActionContext ctx);
    boolean rollback(BusinessActionContext ctx);
}
