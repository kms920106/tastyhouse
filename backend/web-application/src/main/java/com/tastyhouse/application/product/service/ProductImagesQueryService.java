package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductImagesQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductQueryPort;

@Service
@Transactional(readOnly = true)
class ProductImagesQueryService implements ProductImagesQueryUseCase {

    private final ProductQueryPort productQueryPort;
    private final ProductDetailReader productDetailReader;

    public ProductImagesQueryService(ProductQueryPort productQueryPort, ProductDetailReader productDetailReader) {
        this.productQueryPort = productQueryPort;
        this.productDetailReader = productDetailReader;
    }

    @Override
    public List<String> findProductImages(Long productId) {
        productDetailReader.read(productId);
        return productQueryPort.findProductImageUrls(productId);
    }
}
