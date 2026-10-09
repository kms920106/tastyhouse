package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductKeywordSearchQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductStorefrontQueryPort;
import com.tastyhouse.application.product.port.out.SearchProductItemResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ProductKeywordSearchQueryService implements ProductKeywordSearchQueryUseCase {

    private final ProductStorefrontQueryPort productStorefrontQueryPort;

    public ProductKeywordSearchQueryService(ProductStorefrontQueryPort productStorefrontQueryPort) {
        this.productStorefrontQueryPort = productStorefrontQueryPort;
    }

    @Override
    public PageResult<SearchProductItemResult> searchByKeyword(String keyword, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);
        return productStorefrontQueryPort.searchByKeyword(keyword, ProductExposureWindows.now(), pageQuery);
    }
}
