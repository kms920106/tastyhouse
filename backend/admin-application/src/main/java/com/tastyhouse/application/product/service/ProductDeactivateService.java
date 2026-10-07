package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.in.ProductDeactivateCommand;
import com.tastyhouse.application.product.port.in.ProductDeactivateUseCase;

@Service
@Transactional
class ProductDeactivateService implements ProductDeactivateUseCase {

    private final ProductRegistrationService productRegistrationService;

    public ProductDeactivateService(ProductRegistrationService productRegistrationService) {
        this.productRegistrationService = productRegistrationService;
    }

    @Override
    public void deactivateProduct(ProductDeactivateCommand command) {
        Long id = command.productId();
        ProductId productId = ProductId.of(id);
        productRegistrationService.deactivateProduct(productId);
    }
}
