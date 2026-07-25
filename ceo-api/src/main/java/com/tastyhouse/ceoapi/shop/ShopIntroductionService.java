package com.tastyhouse.ceoapi.shop;

import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.shop.domain.model.ShopOwnerMessageHistory;
import com.tastyhouse.core.domain.shop.application.ProhibitedWordValidator;
import com.tastyhouse.core.domain.shop.application.ShopCommandService;
import com.tastyhouse.core.domain.shop.application.ShopQueryService;
import com.tastyhouse.ceoapi.shop.response.ShopIntroductionResponse;
import com.tastyhouse.ceoapi.shop.response.ShopIntroductionValidationResponse;

/**
 * 점주용 가게소개(사장님 한마디) 관리 중개 서비스. 컨트롤러↔core 위임과 소유권 검증만 담당한다.
 */
@Service
@RequiredArgsConstructor
public class ShopIntroductionService {

    private final ShopQueryService shopQueryService;
    private final ShopCommandService shopCommandService;
    private final ProhibitedWordValidator prohibitedWordValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopIntroductionResponse getIntroduction(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String message = shopQueryService.findLatestOwnerMessage(shopId)
            .map(ShopOwnerMessageHistory::getMessage)
            .orElse(null);
        return ShopIntroductionResponse.from(message);
    }

    public void updateIntroduction(Long ceoId, Long shopId, String message) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopCommandService.createOwnerMessage(shopId, message);
    }

    public ShopIntroductionValidationResponse validateIntroduction(Long ceoId, Long shopId, String message) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        List<String> violations = prohibitedWordValidator.findViolations(message);
        return ShopIntroductionValidationResponse.from(violations.isEmpty(), violations);
    }
}
