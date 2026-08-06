package com.bhagwat.springcommerce.product.service.impl;

import com.bhagwat.springcommerce.category.entity.Category;
import com.bhagwat.springcommerce.category.repository.CategoryRepository;
import com.bhagwat.springcommerce.common.exception.ProductAlreadyExistsException;
import com.bhagwat.springcommerce.common.exception.ResourceNotFoundException;
import com.bhagwat.springcommerce.product.dto.ProductRequest;
import com.bhagwat.springcommerce.product.dto.ProductResponse;
import com.bhagwat.springcommerce.product.entity.Product;
import com.bhagwat.springcommerce.product.mapper.ProductMapper;
import com.bhagwat.springcommerce.product.repository.ProductRepository;
import com.bhagwat.springcommerce.product.service.ProductService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final CategoryRepository categoryRepository;

    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productMapper = productMapper;
    }

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        if(productRepository.findByName(request.name()).isPresent()) {
            throw new ProductAlreadyExistsException(request.name());
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category",
                        "id",
                        request.categoryId()));

        Product product = productMapper.toEntity(request, category);

        Product savedProduct = productRepository.save(product);

        return productMapper.toResponse(savedProduct);
    }

    @Override
    @Transactional
    public Page<ProductResponse> getAllProducts(
            int page,
            int size,
            String sortBy,
            String direction)
    {
        Sort sort = direction.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        PageRequest pageRequest = PageRequest.of(page, size, sort);
        Page<Product> products = productRepository.findAll(pageRequest);

        return products.map(productMapper::toResponse);
    }

    @Override
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        return productMapper.toResponse(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProductById(ProductRequest request, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        Optional<Product> existingProduct = productRepository.findByName(request.name());

        if (existingProduct.isPresent()
                && !existingProduct.get().getId().equals(product.getId())){
            throw new ProductAlreadyExistsException(request.name());
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.categoryId()));

        // Update the product
        product.setActive(request.active());
        product.setPrice(request.price());
        product.setCategory(category);
        product.setStockQuantity(request.stockQuantity());
        product.setImageUrl(request.imageUrl());
        product.setDescription(request.description());
        product.setName(request.name());

        Product updatedProduct = productRepository.save(product);
        return productMapper.toResponse(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        Product product = productRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));

        productRepository.delete(product);
    }

}
