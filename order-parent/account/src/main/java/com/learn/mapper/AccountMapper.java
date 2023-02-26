package com.learn.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.entity.Account;

import java.math.BigDecimal;

public interface AccountMapper extends BaseMapper<Account> {
    //扣减账户金额
    void decrease(Long userId, BigDecimal money);

    //查询账户，用来判断是否金额不足
    Account selectByUserId(Long userId);

    //可用金额 --> 冻结金额
    void updateResidueToFrozen(Long userId,BigDecimal money);

    // 冻结 --> 已使用
    void updateFrozenToUsed(Long userId, BigDecimal money);

    // 冻结 --> 可用
    void updateFrozenToResidue(Long userId, BigDecimal money);
}
