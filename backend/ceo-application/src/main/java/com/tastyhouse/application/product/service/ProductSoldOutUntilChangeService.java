package com.tastyhouse.application.product.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductSoldOutUntilChangeCommand;
import com.tastyhouse.application.product.port.in.ProductSoldOutUntilChangeUseCase;
import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductSoldOutUntilChangeService implements ProductSoldOutUntilChangeUseCase {

    private final ProductAvailabilityService productAvailabilityService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductSoldOutUntilChangeService(
        ProductAvailabilityService productAvailabilityService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productAvailabilityService = productAvailabilityService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductAvailabilityChangeView changeProductsSoldOutUntil(ProductSoldOutUntilChangeCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();
        LocalDateTime soldOutUntil = command.soldOutUntil();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productAvailabilityService.changeProductsSoldOutUntil(
            ShopId.of(shopId), toProductIds(productIds), soldOutUntil, LocalDateTime.now()));
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
