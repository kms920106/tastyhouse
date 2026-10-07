package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductCategoryByShopQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductQueryPort;

@Service
@Transactional(readOnly = true)
class ProductCategoryByShopQueryService implements ProductCategoryByShopQueryUseCase {

    private final ProductQueryPort productQueryPort;

    public ProductCategoryByShopQueryService(ProductQueryPort productQueryPort) {
        this.productQueryPort = productQueryPort;
    }

    @Override
    public List<ProductCategoryResult> findShopProductCategories(Long shopId) {
        return productQueryPort.findProductCategories(shopId);
    }
}
