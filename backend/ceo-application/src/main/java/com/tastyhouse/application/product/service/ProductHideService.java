package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductHideCommand;
import com.tastyhouse.application.product.port.in.ProductHideUseCase;
import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductHideService implements ProductHideUseCase {

    private final ProductAvailabilityService productAvailabilityService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductHideService(
        ProductAvailabilityService productAvailabilityService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productAvailabilityService = productAvailabilityService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductAvailabilityChangeView hideProducts(ProductHideCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productAvailabilityService.hideProducts(ShopId.of(shopId), toProductIds(productIds)));
    }

    private ProductAvailabilityChangeView toChangeView(ProductAvailabilityChangeResult result) {
        return new ProductAvailabilityChangeView(
            result.succeeded(),
            result.failed().stream()
                .map(failure -> new ProductAvailabilityChangeView.Failure(
                    failure.id(),
                    failure.name(),
                    failure.errorCode().getCode(),
                    failure.errorCode().getDefaultMessage()
                ))
                .toList()
        );
    }

    private List<ProductId> toProductIds(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }
        return productIds.stream().map(ProductId::of).toList();
    }
}
