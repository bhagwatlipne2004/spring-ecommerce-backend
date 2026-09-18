package com.bhagwat.springcommerce.review.repository;

import com.bhagwat.springcommerce.product.entity.Product;
import com.bhagwat.springcommerce.review.entity.Review;
import com.bhagwat.springcommerce.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository
        extends JpaRepository<Review, Long> {

    List<Review> findByProduct(Product product);

    Optional<Review> findByUserAndProduct(
            User user,
            Product product
    );

    boolean existsByUserAndProduct(User user, Product product);
}