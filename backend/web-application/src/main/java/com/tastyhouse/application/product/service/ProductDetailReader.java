package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Component
class ProductDetailReader {

    private final ProductQueryPort productQueryPort;

    public ProductDetailReader(ProductQueryPort productQueryPort) {
        this.productQueryPort = productQueryPort;
    }

    ProductDetailResult read(Long productId) {
        return productQueryPort.findProductDetailById(productId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
    }
}
