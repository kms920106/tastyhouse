package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductOptionType;
import com.tastyhouse.application.product.port.in.ProductOptionAvailabilityListQueryUseCase;
import com.tastyhouse.application.product.port.out.ProductAvailabilityQueryPort;
import com.tastyhouse.application.product.port.out.ProductAvailabilitySearchCondition;
import com.tastyhouse.application.product.port.out.ProductOptionAvailabilityGroupResult;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional(readOnly = true)
class ProductOptionAvailabilityListQueryService implements ProductOptionAvailabilityListQueryUseCase {

    private final ProductAvailabilityQueryPort productAvailabilityQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionAvailabilityListQueryService(
        ProductAvailabilityQueryPort productAvailabilityQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productAvailabilityQueryPort = productAvailabilityQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ProductOptionAvailabilityGroupResult> getProductOptionAvailability(
        Long ceoId,
        Long shopId,
        String keyword,
        Boolean soldOutOnly,
        Boolean hiddenOnly
    ) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ProductAvailabilitySearchCondition condition =
            ProductAvailabilitySearchCondition.of(shopId, keyword, soldOutOnly, hiddenOnly);

        return productAvailabilityQueryPort.findProductOptionAvailability(
            condition,
            ProductOptionType.NORMAL.name(),
            ProductOptionType.COMMON.name()
        );
    }
}
