package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.application.shop.port.in.ShopRiderVisitGuideOwnerValidationQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopVisitGuideValidationResult;

@Service
@Transactional(readOnly = true)
class ShopRiderVisitGuideOwnerValidationQueryService implements ShopRiderVisitGuideOwnerValidationQueryUseCase {

    private final ShopRiderGuideValidator shopRiderGuideValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRiderVisitGuideOwnerValidationQueryService(
        ShopRiderGuideValidator shopRiderGuideValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRiderGuideValidator = shopRiderGuideValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopVisitGuideValidationResult validateVisitGuide(Long ceoId, Long shopId, String visitGuide) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<String> violations = shopRiderGuideValidator.findViolations(shop, visitGuide);
        return new ShopVisitGuideValidationResult(violations.isEmpty(), violations);
    }
}
