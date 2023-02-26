package com.learn.service.Impl;

import com.alibaba.fastjson.JSONObject;
import com.learn.entity.EasyIdReturnResult;
import com.learn.entity.Order;
import com.learn.feign.AccountClient;
import com.learn.feign.EasyIdClient;
import com.learn.feign.StorageClient;
import com.learn.mapper.OrderMapper;
import com.learn.service.OrderService;
import com.learn.tcc.OrderTccAction;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private EasyIdClient easyIdClient;

    @Autowired
    private AccountClient accountClient;

    @Autowired
    private StorageClient storageClient;

    @Autowired
    private OrderTccAction tcc;

    @GlobalTransactional //启动全局事务
    @Override
    public void create(Order order) {
        //远程调用easy-id-generator发号器，生成id
        String s = easyIdClient.nextId("order_business");
        JSONObject jsonObject = JSONObject.parseObject(s);
        EasyIdReturnResult result = jsonObject.toJavaObject(EasyIdReturnResult.class);

        Long id = Long.valueOf(result.getData());

        order.setId(id);
        //创建订单
//        orderMapper.create(order);
        //冻结订单
        tcc.prepare(null, order.getId(), order.getUserId(), order.getProductId(), order.getCount(), order.getMoney());


        //远程调用库存，减少库存
        storageClient.decrease(order.getProductId(),order.getCount());

        //远程调用账户，扣减账户
        accountClient.decrease(order.getUserId(), order.getMoney());
    }

}
