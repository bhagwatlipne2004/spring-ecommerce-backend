package com.bhagwat.springcommerce.cart.repository;

import com.bhagwat.springcommerce.cart.entity.Cart;
import com.bhagwat.springcommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

}