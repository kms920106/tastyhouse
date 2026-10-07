package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductByShopQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;

@Service
@Transactional(readOnly = true)
class ProductByShopQueryService implements ProductByShopQueryUseCase {

    private final ProductQueryPort productQueryPort;

    public ProductByShopQueryService(ProductQueryPort productQueryPort) {
        this.productQueryPort = productQueryPort;
    }

    @Override
    public List<ShopProductItemResult> findShopProducts(Long shopId) {
        return productQueryPort.findShopProducts(shopId, ProductExposureWindows.now());
    }
}
