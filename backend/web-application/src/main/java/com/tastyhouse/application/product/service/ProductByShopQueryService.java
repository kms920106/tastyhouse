package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductByShopQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductStorefrontQueryPort;
import com.tastyhouse.application.product.port.out.ShopProductItemResult;

@Service
@Transactional(readOnly = true)
class ProductByShopQueryService implements ProductByShopQueryUseCase {

    private final ProductStorefrontQueryPort productStorefrontQueryPort;

    public ProductByShopQueryService(ProductStorefrontQueryPort productStorefrontQueryPort) {
        this.productStorefrontQueryPort = productStorefrontQueryPort;
    }

    @Override
    public List<ShopProductItemResult> findShopProducts(Long shopId) {
        return productStorefrontQueryPort.findShopProducts(shopId, ProductExposureWindows.now());
    }
}
