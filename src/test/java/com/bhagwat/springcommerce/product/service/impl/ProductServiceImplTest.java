package com.bhagwat.springcommerce.product.service.impl;

import com.bhagwat.springcommerce.category.entity.Category;
import com.bhagwat.springcommerce.category.repository.CategoryRepository;
import com.bhagwat.springcommerce.common.exception.ResourceNotFoundException;
import com.bhagwat.springcommerce.product.dto.ProductRequest;
import com.bhagwat.springcommerce.product.dto.ProductResponse;
import com.bhagwat.springcommerce.product.entity.Product;
import com.bhagwat.springcommerce.product.mapper.ProductMapper;
import com.bhagwat.springcommerce.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.bhagwat.springcommerce.common.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


import com.bhagwat.springcommerce.common.exception.ProductAlreadyExistsException;
import org.junit.jupiter.api.Assertions;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_shouldThrowException_whenCategoryDoesNotExist() {
        ProductRequest request = new ProductRequest(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2499.00"),
                20,
                "keyboard.jpg",
                true,
                1L
        );

        when(productRepository.findByName("Keyboard"))
                .thenReturn(Optional.empty());

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception =
                Assertions.assertThrows(
                        ResourceNotFoundException.class,
                        () -> productService.createProduct(request)
                );

        assertEquals("Category with id 1 not found.", exception.getMessage());

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_shouldReturnProduct_whenProductExists() {
        Long productId = 10L;

        Product product = new Product();
        product.setId(productId);
        product.setName("Keyboard");

        ProductResponse expectedResponse = new ProductResponse(
                productId,
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2499.00"),
                20,
                "keyboard.jpg",
                true,
                null
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));
        when(productMapper.toResponse(product))
                .thenReturn(expectedResponse);

        ProductResponse actualResponse = productService.getProductById(productId);

        assertEquals(expectedResponse, actualResponse);
        verify(productRepository).findById(productId);
        verify(productMapper).toResponse(product);
    }

    @Test
    void updateProductById_shouldUpdateProduct_whenValidRequest() {
        Long productId = 10L;

        ProductRequest request = new ProductRequest(
                "Updated Keyboard",
                "Updated mechanical keyboard",
                new BigDecimal("2999.00"),
                15,
                "updated-keyboard.jpg",
                true,
                1L
        );

        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Keyboard");
        existingProduct.setPrice(new BigDecimal("2499.00"));
        existingProduct.setStockQuantity(20);
        existingProduct.setActive(true);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));
        when(productRepository.findByName("Updated Keyboard"))
                .thenReturn(Optional.empty());
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));
        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);

        ProductResponse expectedResponse = new ProductResponse(
                productId,
                "Updated Keyboard",
                "Updated mechanical keyboard",
                new BigDecimal("2999.00"),
                15,
                "updated-keyboard.jpg",
                true,
                null
        );

        when(productMapper.toResponse(existingProduct))
                .thenReturn(expectedResponse);

        ProductResponse actualResponse =
                productService.updateProductById(request, productId);

        assertEquals(expectedResponse, actualResponse);
        assertEquals("Updated Keyboard", existingProduct.getName());
        assertEquals(new BigDecimal("2999.00"), existingProduct.getPrice());
        assertEquals(15, existingProduct.getStockQuantity());

        verify(productRepository).save(existingProduct);
    }

    @Test
    void getProductById_shouldThrowException_whenProductDoesNotExist() {
        Long productId = 999L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getProductById(productId)
        );

        assertEquals("Product with id 999 not found.", exception.getMessage());
        verify(productMapper, never()).toResponse(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowException_whenProductAlreadyExists() {
        ProductRequest request = new ProductRequest(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2499.00"),
                20,
                "keyboard.jpg",
                true,
                1L
        );

        when(productRepository.findByName("Keyboard"))
                .thenReturn(Optional.of(new Product()));

        ProductAlreadyExistsException exception =
                Assertions.assertThrows(
                        ProductAlreadyExistsException.class,
                        () -> productService.createProduct(request)
                );

        assertEquals("Product Keyboard already exists.", exception.getMessage());

        verify(categoryRepository, never()).findById(anyLong());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_shouldReturnProductResponse_whenValidRequest() {
        // Arrange
        ProductRequest request = new ProductRequest(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2499.00"),
                20,
                "keyboard.jpg",
                true,
                1L
        );

        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product product = new Product();
        product.setName("Keyboard");
        product.setCategory(category);
        product.setPrice(new BigDecimal("2499.00"));
        product.setStockQuantity(20);
        product.setActive(true);

        Product savedProduct = new Product();
        savedProduct.setId(10L);
        savedProduct.setName("Keyboard");
        savedProduct.setCategory(category);
        savedProduct.setPrice(new BigDecimal("2499.00"));
        savedProduct.setStockQuantity(20);
        savedProduct.setActive(true);

        ProductResponse expectedResponse = new ProductResponse(
                10L,
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2499.00"),
                20,
                "keyboard.jpg",
                true,
                null
        );

        when(productRepository.findByName("Keyboard"))
                .thenReturn(Optional.empty());
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));
        when(productMapper.toEntity(request, category))
                .thenReturn(product);
        when(productRepository.save(product))
                .thenReturn(savedProduct);
        when(productMapper.toResponse(savedProduct))
                .thenReturn(expectedResponse);

        // Act
        ProductResponse actualResponse = productService.createProduct(request);

        // Assert
        assertEquals(expectedResponse, actualResponse);
        verify(productRepository).save(product);
        verify(productRepository).findByName("Keyboard");
        verify(categoryRepository).findById(1L);
    }

    @Test
    void updateProductById_shouldThrowException_whenProductDoesNotExist() {
        Long productId = 999L;

        ProductRequest request = new ProductRequest(
                "Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2499.00"),
                20,
                "keyboard.jpg",
                true,
                1L
        );

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProductById(request, productId)
        );

        assertEquals("Product with id 999 not found.", exception.getMessage());

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductById_shouldThrowException_whenNameBelongsToAnotherProduct() {
        Long productId = 10L;

        ProductRequest request = new ProductRequest(
                "Mouse",
                "Wireless mouse",
                new BigDecimal("999.00"),
                10,
                "mouse.jpg",
                true,
                1L
        );

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Keyboard");

        Product otherProduct = new Product();
        otherProduct.setId(20L);
        otherProduct.setName("Mouse");

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));
        when(productRepository.findByName("Mouse"))
                .thenReturn(Optional.of(otherProduct));

        ProductAlreadyExistsException exception = Assertions.assertThrows(
                ProductAlreadyExistsException.class,
                () -> productService.updateProductById(request, productId)
        );

        assertEquals("Product Mouse already exists.", exception.getMessage());

        verify(productRepository, never()).save(any(Product.class));
        verify(categoryRepository, never()).findById(anyLong());
    }

    @Test
    void deleteById_shouldDeleteProduct_whenProductExists() {
        Long productId = 10L;

        Product product = new Product();
        product.setId(productId);
        product.setName("Keyboard");

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        productService.deleteById(productId);

        verify(productRepository).delete(product);
    }


    @Test
    void deleteById_shouldThrowException_whenProductDoesNotExist() {
        Long productId = 999L;

        when(productRepository.findById(productId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> productService.deleteById(productId)
        );

        assertEquals("Product with id 999 not found.", exception.getMessage());

        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void getAllProducts_shouldReturnPaginatedProducts() {
        Product product = new Product();
        product.setId(1L);
        product.setName("Keyboard");

        ProductResponse response = new ProductResponse(
                1L, "Keyboard", "Mechanical keyboard",
                new BigDecimal("2499.00"), 20, "keyboard.jpg", true, null
        );

        Page<Product> productPage = new PageImpl<>(
                List.of(product),
                org.springframework.data.domain.PageRequest.of(
                        0, 10, org.springframework.data.domain.Sort.by("name").ascending()),
                1
        );

        when(productRepository.findAll(
                any(Specification.class), any(Pageable.class)))
                .thenReturn(productPage);

        when(productMapper.toResponse(product)).thenReturn(response);

        PageResponse<ProductResponse> result = productService.getAllProducts(
                0, 10, "name", "asc", null, null, null
        );

        assertEquals(1, result.content().size());
        assertEquals("Keyboard", result.content().get(0).name());
        assertEquals(0, result.page());
        assertEquals(10, result.size());
        assertEquals(1, result.totalElements());
        assertEquals(1, result.totalPages());
        assertEquals(true, result.first());
        assertEquals(true, result.last());
    }

    @Test
    void getAllProducts_shouldFilterByCategory() {
        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        when(productRepository.findAll(
                any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        PageResponse<ProductResponse> result = productService.getAllProducts(
                0, 10, "name", "asc", 1L, null, null
        );

        assertEquals(0, result.totalElements());
        verify(categoryRepository).findById(1L);
    }

    @Test
    void getAllProducts_shouldAcceptKeywordFilter() {
        when(productRepository.findAll(
                any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        PageResponse<ProductResponse> result = productService.getAllProducts(
                0, 10, "name", "asc", null, "key", null
        );

        assertEquals(0, result.totalElements());
        verify(productRepository).findAll(
                any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllProducts_shouldAcceptActiveFilter() {
        when(productRepository.findAll(
                any(Specification.class), any(Pageable.class)))
                .thenReturn(Page.empty());

        PageResponse<ProductResponse> result = productService.getAllProducts(
                0, 10, "name", "desc", null, null, false
        );

        assertEquals(0, result.totalElements());
        verify(productRepository).findAll(
                any(Specification.class), any(Pageable.class));
    }

    @Test
    void getAllProducts_shouldThrowException_whenCategoryDoesNotExist() {
        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> productService.getAllProducts(
                        0, 10, "name", "asc", 999L, null, null
                )
        );

        assertEquals("Category with id 999 not found.", exception.getMessage());

        verify(productRepository, never()).findAll(
                any(Specification.class), any(Pageable.class));
    }

    @Test
    void updateProductById_shouldThrowException_whenCategoryDoesNotExist() {
        Long productId = 10L;

        ProductRequest request = new ProductRequest(
                "Updated Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2999.00"),
                15,
                "keyboard.jpg",
                true,
                999L
        );

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Keyboard");

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));
        when(productRepository.findByName("Updated Keyboard"))
                .thenReturn(Optional.empty());
        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = Assertions.assertThrows(
                ResourceNotFoundException.class,
                () -> productService.updateProductById(request, productId)
        );

        assertEquals("Category with id 999 not found.", exception.getMessage());
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProductById_shouldPreserveActive_whenRequestActiveIsNull() {
        Long productId = 10L;

        ProductRequest request = new ProductRequest(
                "Updated Keyboard",
                "Mechanical keyboard",
                new BigDecimal("2999.00"),
                15,
                "keyboard.jpg",
                null,
                1L
        );

        Category category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        Product existingProduct = new Product();
        existingProduct.setId(productId);
        existingProduct.setName("Keyboard");
        existingProduct.setPrice(new BigDecimal("2499.00"));
        existingProduct.setStockQuantity(20);
        existingProduct.setActive(true);

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(existingProduct));
        when(productRepository.findByName("Updated Keyboard"))
                .thenReturn(Optional.empty());
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));
        when(productRepository.save(existingProduct))
                .thenReturn(existingProduct);

        when(productMapper.toResponse(existingProduct))
                .thenAnswer(invocation -> new ProductResponse(
                        existingProduct.getId(),
                        existingProduct.getName(),
                        existingProduct.getDescription(),
                        existingProduct.getPrice(),
                        existingProduct.getStockQuantity(),
                        existingProduct.getImageUrl(),
                        existingProduct.getActive(),
                        null
                ));

        ProductResponse result =
                productService.updateProductById(request, productId);

        assertEquals(true, existingProduct.getActive());
        assertEquals("Updated Keyboard", result.name());
        assertEquals(true, result.active());
        verify(productRepository).save(existingProduct);
    }

}