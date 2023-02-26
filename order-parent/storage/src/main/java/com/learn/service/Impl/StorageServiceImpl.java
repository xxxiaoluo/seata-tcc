package com.learn.service.Impl;

import com.learn.service.StorageService;
import com.learn.tcc.StorageTccAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageServiceImpl implements StorageService {
    @Autowired
    private StorageTccAction tcc;

    @Override
    public void decrease(Long productId, Integer count) {
        tcc.prepare(null, productId, count);
    }
}