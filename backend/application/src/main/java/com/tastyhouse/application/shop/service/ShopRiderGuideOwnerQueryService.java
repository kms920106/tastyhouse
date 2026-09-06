package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopRiderGuideOwnerQueryUseCase;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.service.ShopRiderGuideValidator;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.ShopVisitGuideValidationResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopRiderGuideOwnerQueryService implements ShopRiderGuideOwnerQueryUseCase {

    private final ShopRiderGuideQueryPort shopRiderGuideQueryPort;
    private final ShopRiderGuideValidator shopRiderGuideValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRiderGuideOwnerQueryService(
        ShopRiderGuideQueryPort shopRiderGuideQueryPort,
        ShopRiderGuideValidator shopRiderGuideValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRiderGuideQueryPort = shopRiderGuideQueryPort;
        this.shopRiderGuideValidator = shopRiderGuideValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopRiderGuideResult getRiderGuide(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopRiderGuideQueryPort.findRiderGuide(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
    }

    @Override
    public ShopVisitGuideValidationResult validateVisitGuide(Long ceoId, Long shopId, String visitGuide) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<String> violations = shopRiderGuideValidator.findViolations(shop, visitGuide);
        return new ShopVisitGuideValidationResult(violations.isEmpty(), violations);
    }
}
