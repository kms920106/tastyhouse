package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductTodayDiscountQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductQueryPort;
import com.tastyhouse.application.product.port.out.TodayDiscountProductResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ProductTodayDiscountQueryService implements ProductTodayDiscountQueryUseCase {

    private final ProductQueryPort productQueryPort;

    public ProductTodayDiscountQueryService(ProductQueryPort productQueryPort) {
        this.productQueryPort = productQueryPort;
    }

    @Override
    public PageResult<TodayDiscountProductResult> searchTodayDiscountProducts(int page, int size) {
        return productQueryPort.findTodayDiscountProducts(ProductExposureWindows.now(), PageQuery.of(page, size));
    }
}
