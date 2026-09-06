package com.tastyhouse.application.product.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Component;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.product.model.ProductOption;
import com.tastyhouse.domain.product.model.ProductOptionGroup;
import com.tastyhouse.domain.product.repository.ProductOptionGroupRepository;
import com.tastyhouse.domain.product.repository.ProductOptionRepository;
import com.tastyhouse.domain.product.service.ProductOptionGroupLinkService;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;

@Component
@CeoApp
public class ProductOptionGroupOwnershipValidator {

    private final ProductOptionGroupRepository productOptionGroupRepository;
    private final ProductOptionRepository productOptionRepository;
    private final ProductOptionGroupLinkService productOptionGroupLinkService;

    public ProductOptionGroupOwnershipValidator(
        ProductOptionGroupRepository productOptionGroupRepository,
        ProductOptionRepository productOptionRepository,
        ProductOptionGroupLinkService productOptionGroupLinkService
    ) {
        this.productOptionGroupRepository = productOptionGroupRepository;
        this.productOptionRepository = productOptionRepository;
        this.productOptionGroupLinkService = productOptionGroupLinkService;
    }

    public ProductOptionGroup loadOwnedOptionGroup(Long shopId, Long optionGroupId) {
        ProductOptionGroup group = productOptionGroupRepository.findById(ProductOptionGroupId.of(optionGroupId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_OPTION_GROUP_NOT_FOUND));
        validateOptionGroupShop(shopId, optionGroupId);
        return group;
    }

    public ProductOption loadOwnedOption(Long shopId, Long optionId) {
        ProductOption option = productOptionRepository.findById(ProductOptionId.of(optionId))
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
