package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductCategory;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductCategoryCreateUseCase;
import com.tastyhouse.application.product.port.in.ProductCategoryManagementCreateCommand;

@Service
@Transactional
class ProductCategoryCreateService implements ProductCategoryCreateUseCase {

    private final ProductRegistrationService productRegistrationService;

    public ProductCategoryCreateService(ProductRegistrationService productRegistrationService) {
        this.productRegistrationService = productRegistrationService;
    }

    @Override
    public Long createProductCategory(ProductCategoryManagementCreateCommand command) {
        Long shopId = command.shopId();
        String name = command.name();
        Integer sort = command.sort();
        boolean visible = command.visible();

        ProductCategory category = productRegistrationService.createProductCategory(
            ShopId.of(shopId), name, null, sort, visible
        );
        return category.getId();
    }
}
