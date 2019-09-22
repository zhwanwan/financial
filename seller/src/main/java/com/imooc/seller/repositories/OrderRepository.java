package com.imooc.seller.repositories;

import com.imooc.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * 订单管理
 */
public interface OrderRepository extends CrudRepository<Order, String>, JpaRepository<Order, String>, JpaSpecificationExecutor<Order> {
}