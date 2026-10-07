package com.tastyhouse.application.product.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ReleaseTarget;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductReleaseCommand;
import com.tastyhouse.application.product.port.in.ProductReleaseUseCase;
import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductReleaseService implements ProductReleaseUseCase {

    private final ProductAvailabilityService productAvailabilityService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductReleaseService(
        ProductAvailabilityService productAvailabilityService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productAvailabilityService = productAvailabilityService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductAvailabilityChangeView releaseProducts(ProductReleaseCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> productIds = command.productIds();
        String target = command.target();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productAvailabilityService.releaseProducts(
            ShopId.of(shopId), toProductIds(productIds), ReleaseTarget.from(target)));
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
