package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopNoticeOwnerValidationQueryUseCase;

@Service
@Transactional(readOnly = true)
class ShopNoticeOwnerValidationQueryService implements ShopNoticeOwnerValidationQueryUseCase {

    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProhibitedWordValidator prohibitedWordValidator;

    public ShopNoticeOwnerValidationQueryService(
        ShopOwnershipValidator shopOwnershipValidator,
        ProhibitedWordValidator prohibitedWordValidator
    ) {
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.prohibitedWordValidator = prohibitedWordValidator;
    }

    @Override
    public List<String> validateNotice(Long ceoId, Long shopId, String content) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return prohibitedWordValidator.findViolations(content);
    }
}
