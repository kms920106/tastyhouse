package com.tastyhouse.application.product.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.out.write.ProductOptionGroupLoadPort;
import com.tastyhouse.application.product.port.out.write.ProductOptionLoadPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.CeoErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Component
public class ProductOptionGroupOwnershipValidator {

    private final ProductOptionGroupLoadPort productOptionGroupLoadPort;
    private final ProductOptionLoadPort productOptionLoadPort;
    private final ProductOptionGroupLinkService productOptionGroupLinkService;

    public ProductOptionGroupOwnershipValidator(
        ProductOptionGroupLoadPort productOptionGroupLoadPort,
        ProductOptionLoadPort productOptionLoadPort,
        ProductOptionGroupLinkService productOptionGroupLinkService
    ) {
        this.productOptionGroupLoadPort = productOptionGroupLoadPort;
        this.productOptionLoadPort = productOptionLoadPort;
        this.productOptionGroupLinkService = productOptionGroupLinkService;
    }

    public ProductOptionGroup loadOwnedOptionGroup(Long shopId, Long optionGroupId) {
        ProductOptionGroup group = productOptionGroupLoadPort.findById(ProductOptionGroupId.of(optionGroupId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
        validateOptionGroupShop(shopId, optionGroupId);
        return group;
    }

    public ProductOption loadOwnedOption(Long shopId, Long optionId) {
        ProductOption option = productOptionLoadPort.findById(ProductOptionId.of(optionId))
            .orElseThrow(() -> new ResourceNotFoundException(CeoErrorCode.PRODUCT_OPTION_NOT_FOUND));

        if (doesNotOwnOptionGroup(shopId, option.getOptionGroupId().value())) {
            throw new ResourceNotFoundException(CeoErrorCode.PRODUCT_OPTION_NOT_FOUND);
        }
        return option;
    }

    public void validateOptionGroupShop(Long shopId, Long optionGroupId) {
        if (doesNotOwnOptionGroup(shopId, optionGroupId)) {
            throw new ResourceNotFoundException(ApplicationErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND);
        }
    }

    private boolean doesNotOwnOptionGroup(Long shopId, Long optionGroupId) {
        ShopId owner = productOptionGroupLinkService.findOwningShopId(ProductOptionGroupId.of(optionGroupId));
        return owner == null || !owner.equals(ShopId.of(shopId));
    }
}
