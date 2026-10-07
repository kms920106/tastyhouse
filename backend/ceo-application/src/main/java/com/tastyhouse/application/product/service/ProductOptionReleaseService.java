package com.tastyhouse.application.product.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.product.model.ProductOptionType;
import com.tastyhouse.domain.product.model.ReleaseTarget;
import com.tastyhouse.domain.product.vo.ProductCommonOptionId;
import com.tastyhouse.domain.product.vo.ProductOptionId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.product.port.in.ProductOptionReleaseCommand;
import com.tastyhouse.application.product.port.in.ProductOptionReleaseUseCase;
import com.tastyhouse.application.product.port.in.ProductOptionTargetCommand;
import com.tastyhouse.application.product.port.out.ProductAvailabilityChangeView;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shop.service.ShopOwnershipValidator;

@Service
@Transactional
class ProductOptionReleaseService implements ProductOptionReleaseUseCase {

    private final ProductAvailabilityService productAvailabilityService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ProductOptionReleaseService(
        ProductAvailabilityService productAvailabilityService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.productAvailabilityService = productAvailabilityService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ProductAvailabilityChangeView releaseOptions(ProductOptionReleaseCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        List<Long> optionIds = command.options().stream().map(ProductOptionTargetCommand::optionId).toList();
        List<String> optionTypes = command.options().stream().map(ProductOptionTargetCommand::optionType).toList();
        String target = command.target();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toChangeView(productAvailabilityService.releaseOptions(
            ShopId.of(shopId), toOptionIds(optionIds, optionTypes), toCommonOptionIds(optionIds, optionTypes),
            ReleaseTarget.from(target)));
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

    private List<ProductOptionId> toOptionIds(List<Long> optionIds, List<String> optionTypes) {
        return filterByType(optionIds, optionTypes, ProductOptionType.NORMAL).stream()
            .map(ProductOptionId::of)
            .toList();
    }

    private List<ProductCommonOptionId> toCommonOptionIds(List<Long> optionIds, List<String> optionTypes) {
        return filterByType(optionIds, optionTypes, ProductOptionType.COMMON).stream()
            .map(ProductCommonOptionId::of)
            .toList();
    }

    private List<Long> filterByType(List<Long> optionIds, List<String> optionTypes, ProductOptionType wanted) {
        if (optionIds == null || optionIds.isEmpty()) {
            throw new ApplicationException(ApplicationErrorCode.PRODUCT_AVAILABILITY_TARGET_EMPTY);
        }
        List<Long> filtered = new ArrayList<>();
        for (int i = 0; i < optionIds.size(); i++) {
            if (ProductOptionType.from(optionTypes.get(i)) == wanted) {
                filtered.add(optionIds.get(i));
            }
        }
        return filtered;
    }
}
