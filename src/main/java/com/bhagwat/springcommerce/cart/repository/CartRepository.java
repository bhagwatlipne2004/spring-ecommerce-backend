package com.bhagwat.springcommerce.cart.repository;

import com.bhagwat.springcommerce.cart.entity.Cart;
import com.bhagwat.springcommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUser(User user);

    @Query("""
    SELECT DISTINCT c
    FROM Cart c
    LEFT JOIN FETCH c.items
    WHERE c.id = :cartId
    """)
    Optional<Cart> findByIdWithItems(@Param("cartId") Long cartId);
}