package com.learn.service.Impl;

import com.learn.service.AccountService;
import com.learn.tcc.AccountTccAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    private AccountTccAction tcc;

    @Override
    public void decrease(Long userId, BigDecimal money) {
        tcc.prepare(null,userId,money);
    }
}
