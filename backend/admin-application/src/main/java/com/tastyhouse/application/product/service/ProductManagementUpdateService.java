package com.tastyhouse.application.product.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.application.product.port.in.ProductManagementUpdateCommand;
import com.tastyhouse.application.product.port.in.ProductManagementUpdateUseCase;

@Service
@Transactional
class ProductManagementUpdateService implements ProductManagementUpdateUseCase {

    private final ProductRegistrationService productRegistrationService;

    public ProductManagementUpdateService(ProductRegistrationService productRegistrationService) {
        this.productRegistrationService = productRegistrationService;
    }

    @Override
    public void updateProduct(ProductManagementUpdateCommand command) {
        Long id = command.productId();
        Long productCategoryId = command.productCategoryId();
        String name = command.name();
        String description = command.description();
        Integer originalPrice = command.originalPrice();
        Integer discountPrice = command.discountPrice();
        BigDecimal discountRate = command.discountRate();
        boolean representative = command.representative();
        Integer spiciness = command.spiciness();
        boolean soldOut = command.soldOut();
        boolean visible = command.visible();
        Integer sort = command.sort();

        ProductId productId = ProductId.of(id);
        productRegistrationService.updateProduct(
            productId,
            ProductCategoryId.of(productCategoryId),
            name,
            description,
            originalPrice,
            discountPrice,
            discountRate,
            representative,
            spiciness,
            soldOut,
            visible,
            sort
        );
    }
}
