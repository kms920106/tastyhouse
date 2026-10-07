package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopIntroductionValidationQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopIntroductionValidationResult;

@Service
@Transactional(readOnly = true)
class ShopIntroductionValidationQueryService implements ShopIntroductionValidationQueryUseCase {

    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopIntroductionValidationQueryService(
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopIntroductionValidationResult validateIntroduction(Long ceoId, Long shopId, String message) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<String> violations = prohibitedWordValidator.findViolations(message);
        return new ShopIntroductionValidationResult(violations.isEmpty(), violations);
    }
}
