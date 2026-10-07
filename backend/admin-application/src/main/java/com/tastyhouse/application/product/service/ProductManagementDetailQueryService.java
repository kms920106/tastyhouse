package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.product.port.in.ProductManagementDetailQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductDetailResult;
import com.tastyhouse.application.product.port.out.ProductManagementQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class ProductManagementDetailQueryService implements ProductManagementDetailQueryUseCase {

    private final ProductManagementQueryPort productManagementQueryPort;

    public ProductManagementDetailQueryService(ProductManagementQueryPort productManagementQueryPort) {
        this.productManagementQueryPort = productManagementQueryPort;
    }

    @Override
    public ProductDetailResult getProduct(Long id) {
        return productManagementQueryPort.findProductDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_NOT_FOUND));
    }
}
