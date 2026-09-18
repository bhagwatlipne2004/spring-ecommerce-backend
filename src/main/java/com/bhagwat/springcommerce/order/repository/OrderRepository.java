package com.bhagwat.springcommerce.order.repository;

import com.bhagwat.springcommerce.order.entity.Order;
import com.bhagwat.springcommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

}