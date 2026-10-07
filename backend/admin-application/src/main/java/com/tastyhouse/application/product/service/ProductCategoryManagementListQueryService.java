package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductCategoryManagementListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductCategoryResult;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ProductCategoryManagementListQueryService implements ProductCategoryManagementListQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;

    public ProductCategoryManagementListQueryService(ProductManagementQueryPort productManagementQueryPort) {
        this.productManagementQueryPort = productManagementQueryPort;
    }

    @Override
    public List<ProductCategoryResult> getProductCategories(Long shopId) {
        return productManagementQueryPort.findProductCategories(shopId);
    }
}
