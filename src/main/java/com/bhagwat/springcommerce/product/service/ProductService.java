package com.bhagwat.springcommerce.product.service;

import com.bhagwat.springcommerce.product.dto.ProductRequest;
import com.bhagwat.springcommerce.product.dto.ProductResponse;
import com.bhagwat.springcommerce.product.entity.Product;
import org.springframework.data.domain.Page;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request);

    public Page<ProductResponse> getAllProducts(
            int page,
            int size,
            String sortBy,
            String direction
    );

    public ProductResponse getProductById(Long id);

    public ProductResponse updateProductById(ProductRequest request, Long id);

    public void deleteById(Long id);
}
