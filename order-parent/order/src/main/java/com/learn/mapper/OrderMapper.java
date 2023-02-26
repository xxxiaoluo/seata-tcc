package com.learn.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.learn.entity.Order;

public interface OrderMapper extends BaseMapper<Order> {
    // 创建订单
    void create(Order order);

    //TCC新增以下3个方法
    //创建冻结订单
    void createFrozen(Order order);

    //修改订单状态
    void updateStatus(Long orderId,Integer status);

    //删除订单，使用继承的mybatis-plus方法 deleteById()

}
