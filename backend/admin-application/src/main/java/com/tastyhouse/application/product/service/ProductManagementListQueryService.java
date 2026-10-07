package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductManagementListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductListItemResult;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.product.port.out.ProductSearchCondition;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class ProductManagementListQueryService implements ProductManagementListQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;

    public ProductManagementListQueryService(ProductManagementQueryPort productManagementQueryPort) {
        this.productManagementQueryPort = productManagementQueryPort;
    }

    @Override
    public PageResult<ProductListItemResult> getProducts(
        Long shopId,
        Long productCategoryId,
        String name,
        Boolean visible,
        Boolean soldOut,
        int page,
        int size
    ) {
        ProductSearchCondition condition = ProductSearchCondition.of(shopId, productCategoryId, name, visible, soldOut);
        PageQuery pageQuery = PageQuery.of(page, size);
        return productManagementQueryPort.findProducts(condition, pageQuery);
    }
}
