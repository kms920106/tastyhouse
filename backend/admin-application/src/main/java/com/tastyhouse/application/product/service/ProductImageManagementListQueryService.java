package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductImageManagementListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ProductImageManagementListQueryService implements ProductImageManagementListQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;

    public ProductImageManagementListQueryService(ProductManagementQueryPort productManagementQueryPort) {
        this.productManagementQueryPort = productManagementQueryPort;
    }

    @Override
    public List<String> getProductImages(Long id) {
        return productManagementQueryPort.findProductImageUrls(id);
    }
}
