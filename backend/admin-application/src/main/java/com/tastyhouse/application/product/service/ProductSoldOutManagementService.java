package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.in.ProductSoldOutManagementCommand;
import com.tastyhouse.application.product.port.in.ProductSoldOutManagementUseCase;

@Service
@Transactional
class ProductSoldOutManagementService implements ProductSoldOutManagementUseCase {

    private final ProductRegistrationService productRegistrationService;

    public ProductSoldOutManagementService(ProductRegistrationService productRegistrationService) {
        this.productRegistrationService = productRegistrationService;
    }

    @Override
    public void markSoldOut(ProductSoldOutManagementCommand command) {
        Long id = command.productId();
        ProductId productId = ProductId.of(id);
        productRegistrationService.markSoldOut(productId);
    }
}
