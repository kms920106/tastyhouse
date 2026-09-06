package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopIntroductionQueryUseCase;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.port.out.ShopOwnerMessageResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopIntroductionValidationResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopIntroductionQueryService implements ShopIntroductionQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopIntroductionQueryService(
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ProhibitedWordValidator prohibitedWordValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.prohibitedWordValidator = prohibitedWordValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public String getIntroduction(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return shopBasicInfoQueryPort.findLatestOwnerMessage(shopId)
            .map(ShopOwnerMessageResult::message)
            .orElse(null);
    }

    @Override
    public ShopIntroductionValidationResult validateIntroduction(Long ceoId, Long shopId, String message) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<String> violations = prohibitedWordValidator.findViolations(message);
        return new ShopIntroductionValidationResult(violations.isEmpty(), violations);
    }
}
