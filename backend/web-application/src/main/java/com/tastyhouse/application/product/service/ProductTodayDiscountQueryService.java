package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductTodayDiscountQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductStorefrontQueryPort;
import com.tastyhouse.application.product.port.out.TodayDiscountProductResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ProductTodayDiscountQueryService implements ProductTodayDiscountQueryUseCase {

    private final ProductStorefrontQueryPort productStorefrontQueryPort;

    public ProductTodayDiscountQueryService(ProductStorefrontQueryPort productStorefrontQueryPort) {
        this.productStorefrontQueryPort = productStorefrontQueryPort;
    }

    @Override
    public PageResult<TodayDiscountProductResult> searchTodayDiscountProducts(int page, int size) {
        return productStorefrontQueryPort.findTodayDiscountProducts(ProductExposureWindows.now(), PageQuery.of(page, size));
    }
}
