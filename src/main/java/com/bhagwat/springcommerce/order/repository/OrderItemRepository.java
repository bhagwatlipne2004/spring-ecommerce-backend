package com.bhagwat.springcommerce.order.repository;

import com.bhagwat.springcommerce.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

}