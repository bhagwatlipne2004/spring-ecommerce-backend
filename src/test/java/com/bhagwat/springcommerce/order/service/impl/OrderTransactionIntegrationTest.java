
package com.bhagwat.springcommerce.order.service.impl;

import com.bhagwat.springcommerce.cart.entity.Cart;
import com.bhagwat.springcommerce.cart.entity.CartItem;
import com.bhagwat.springcommerce.cart.repository.CartRepository;
import com.bhagwat.springcommerce.category.entity.Category;
import com.bhagwat.springcommerce.category.repository.CategoryRepository;
import com.bhagwat.springcommerce.order.entity.Order;
import com.bhagwat.springcommerce.order.mapper.OrderMapper;
import com.bhagwat.springcommerce.order.repository.OrderRepository;
import com.bhagwat.springcommerce.order.service.OrderService;
import com.bhagwat.springcommerce.product.entity.Product;
import com.bhagwat.springcommerce.product.repository.ProductRepository;
import com.bhagwat.springcommerce.user.entity.Role;
import com.bhagwat.springcommerce.user.entity.User;
import com.bhagwat.springcommerce.user.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
@Testcontainers
class OrderTransactionIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("spring_commerce_test")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver");

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "false");

        registry.add("spring.datasource.hikari.connection-init-sql",
                () -> "SET TIME ZONE 'Asia/Kolkata'");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @MockitoSpyBean
    private OrderMapper orderMapper;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void placeOrderShouldRollbackOrderStockAndCartWhenResponseMappingFails() {
        // Arrange: create a customer.
        User user = new User();
        user.setFirstName("Rollback");
        user.setLastName("Test");
        user.setEmail("rollback-test@example.com");
        user.setPassword("test-password");
        user.setRole(Role.CUSTOMER);
        user.setEnabled(true);
        user = userRepository.saveAndFlush(user);

        // Arrange: create a category and a product with 10 units in stock.
        Category category = new Category(
                null,
                "Rollback Test Category",
                "Integration test category",
                null,
                true
        );
        category = categoryRepository.saveAndFlush(category);

        Product product = Product.builder()
                .category(category)
                .name("Rollback Test Product")
                .description("Product for transaction rollback testing")
                .price(new BigDecimal("500.00"))
                .stockQuantity(10)
                .active(true)
                .build();

        product = productRepository.saveAndFlush(product);

        Long userId = user.getId();
        Long productId = product.getId();

        // Arrange: add 2 units of the product to the customer's cart.
        Cart cart = Cart.builder()
                .user(user)
                .build();

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(2)
                .build();

        cart.getItems().add(cartItem);
        cart = cartRepository.saveAndFlush(cart);

        Long cartId = cart.getId();

        // The real service expects a User object as the authenticated principal.
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        user,
                        null,
                        user.getAuthorities()
                )
        );

        // Force a failure after the service has performed its database changes.
        doThrow(new IllegalStateException("Simulated response-mapping failure"))
                .when(orderMapper)
                .toOrderResponse(any(Order.class));

        // Act: the mapper failure should escape the transactional service method.
        assertThrows(
                IllegalStateException.class,
                () -> orderService.placeOrder()
        );

        // Assert: no order remains for this customer.
        User persistedUser = userRepository.findById(userId).orElseThrow();
        assertTrue(
                orderRepository.findByUserOrderByCreatedAtDesc(persistedUser).isEmpty(),
                "The order should have been rolled back"
        );

        // Assert: product stock should be restored to 10.
        Product persistedProduct = productRepository.findById(productId).orElseThrow();
        assertEquals(
                10,
                persistedProduct.getStockQuantity(),
                "Stock deduction should have been rolled back"
        );

        // Assert: the original cart item and quantity should remain.
        Cart cartWithItems = cartRepository.findByIdWithItems(cartId)
                .orElseThrow();

        assertEquals(1, cartWithItems.getItems().size());
        assertEquals(2, cartWithItems.getItems().get(0).getQuantity());
    }
}
