package com.bhagwat.springcommerce.cart.repository;

import com.bhagwat.springcommerce.cart.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

}