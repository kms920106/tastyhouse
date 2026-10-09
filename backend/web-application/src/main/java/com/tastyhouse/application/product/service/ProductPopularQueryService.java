package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.order.model.OrderStatus;
import com.tastyhouse.application.product.port.in.ProductPopularQueryUseCase;
import com.tastyhouse.application.product.port.out.PopularProductItemResult;
import com.tastyhouse.application.product.port.out.ProductStorefrontQueryPort;

@Service
@Transactional(readOnly = true)
class ProductPopularQueryService implements ProductPopularQueryUseCase {

    private final ProductStorefrontQueryPort productStorefrontQueryPort;

    public ProductPopularQueryService(ProductStorefrontQueryPort productStorefrontQueryPort) {
        this.productStorefrontQueryPort = productStorefrontQueryPort;
    }

    @Override
    public List<PopularProductItemResult> findPopularProducts(Long shopId) {
        return productStorefrontQueryPort.findPopularProducts(
            shopId,
            OrderStatus.COMPLETED.name(),
            ProductExposureWindows.now()
        );
    }
}
