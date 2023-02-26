package com.learn.tcc;

import com.learn.entity.Account;
import com.learn.mapper.AccountMapper;
import io.seata.rm.tcc.api.BusinessActionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Component
public class AccountTccActionImpl implements AccountTccAction {
    @Autowired
    private AccountMapper accountMapper;

    @Transactional
    @Override
    public boolean prepare(BusinessActionContext ctx, Long userId, BigDecimal money) {
        Account account = accountMapper.selectByUserId(userId);
        if(account.getResidue().compareTo(money) < 0){
            throw new RuntimeException("可用金额不足~");
        }
        accountMapper.updateResidueToFrozen(userId, money);
        ResultHolder.setResult(AccountTccAction.class, ctx.getXid(), "flag");
        return true;
    }

    @Transactional
    @Override
    public synchronized boolean commit(BusinessActionContext ctx) {
        if (ResultHolder.getResult(AccountTccAction.class, ctx.getXid()) == null){
            return true;
        }
        Long userId = Long.valueOf(ctx.getActionContext("userId").toString());
        BigDecimal money = new BigDecimal(ctx.getActionContext("money").toString());
        accountMapper.updateFrozenToUsed(userId, money);
        ResultHolder.removeResult(AccountTccAction.class, ctx.getXid());
        return true;
    }

    @Transactional
    @Override
    public synchronized boolean rollback(BusinessActionContext ctx) {
        if (ResultHolder.getResult(AccountTccAction.class, ctx.getXid()) == null){
            return true;
        }
        Long userId = Long.valueOf(ctx.getActionContext("userId").toString());
        BigDecimal money = new BigDecimal(ctx.getActionContext("money").toString());
        accountMapper.updateFrozenToResidue(userId, money);
        ResultHolder.removeResult(AccountTccAction.class, ctx.getXid());
        return true;
    }
}
