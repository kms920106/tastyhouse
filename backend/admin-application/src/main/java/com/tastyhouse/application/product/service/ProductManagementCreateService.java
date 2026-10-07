package com.tastyhouse.application.product.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.Product;
import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductManagementCreateCommand;
import com.tastyhouse.application.product.port.in.ProductManagementCreateUseCase;

@Service
@Transactional
class ProductManagementCreateService implements ProductManagementCreateUseCase {

    private final ProductRegistrationService productRegistrationService;

    public ProductManagementCreateService(ProductRegistrationService productRegistrationService) {
        this.productRegistrationService = productRegistrationService;
    }

    @Override
    public Long createProduct(ProductManagementCreateCommand command) {
        Long shopId = command.shopId();
        Long productCategoryId = command.productCategoryId();
        String name = command.name();
        String description = command.description();
        Integer originalPrice = command.originalPrice();
        Integer discountPrice = command.discountPrice();
        BigDecimal discountRate = command.discountRate();
        Double rating = command.rating();
        Integer reviewCount = command.reviewCount();
        boolean representative = command.representative();
        Integer spiciness = command.spiciness();
        boolean soldOut = command.soldOut();
        boolean visible = command.visible();
        Integer sort = command.sort();

        Product product = productRegistrationService.createProduct(
            ShopId.of(shopId),
            ProductCategoryId.of(productCategoryId),
            name,
            description,
            originalPrice,
            discountPrice,
            discountRate,
            rating,
            reviewCount,
            representative,
            spiciness,
            soldOut,
            visible,
            sort,
            false,
            null,
            false
        );
        return product.getId();
    }
}
