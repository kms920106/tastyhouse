package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupPersistencePort;
import com.tastyhouse.application.product.port.out.write.ProductOptionPersistencePort;

@Component
public class ProductOptionGroupOwnershipValidator {

    private final ProductOptionGroupPersistencePort productOptionGroupPersistencePort;
    private final ProductOptionPersistencePort productOptionPersistencePort;
    private final ProductOptionGroupLinkService productOptionGroupLinkService;

    public ProductOptionGroupOwnershipValidator(
        ProductOptionGroupPersistencePort productOptionGroupPersistencePort,
        ProductOptionPersistencePort productOptionPersistencePort,
        ProductOptionGroupLinkService productOptionGroupLinkService
    ) {
        this.productOptionGroupPersistencePort = productOptionGroupPersistencePort;
        this.productOptionPersistencePort = productOptionPersistencePort;
        this.productOptionGroupLinkService = productOptionGroupLinkService;
    }

    public ProductOptionGroup loadOwnedOptionGroup(Long shopId, Long optionGroupId) {
        ProductOptionGroup group = productOptionGroupPersistencePort.findById(ProductOptionGroupId.of(optionGroupId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
        validateOptionGroupShop(shopId, optionGroupId);
        return group;
    }

    public ProductOption loadOwnedOption(Long shopId, Long optionId) {
        ProductOption option = productOptionPersistencePort.findById(ProductOptionId.of(optionId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_NOT_FOUND));

        if (doesNotOwnOptionGroup(shopId, option.getOptionGroupId().value())) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }
        return option;
    }

    public void validateOptionGroupShop(Long shopId, Long optionGroupId) {
        if (doesNotOwnOptionGroup(shopId, optionGroupId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
        }
    }

    private boolean doesNotOwnOptionGroup(Long shopId, Long optionGroupId) {
        ShopId owner = productOptionGroupLinkService.findOwningShopId(ProductOptionGroupId.of(optionGroupId));
        return owner == null || !owner.equals(ShopId.of(shopId));
    }
}
