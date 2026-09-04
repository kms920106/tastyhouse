package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopRiderGuideOwnerQueryUseCase;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.service.ShopRiderGuideValidator;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.ShopVisitGuideValidationResult;

/**
 * 점주용 라이더 가게방문 안내 조회 서비스(CQRS query 측).
 *
 * <p>아직 한 번도 등록하지 않은 가게도 정상 응답을 돌려준다 — "미등록"은 오류가 아니라 정상 상태이므로
 * 문구·픽업 위치를 null로 채워 내려준다.
 */
@Service
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

    /**
     * 저장 전 위반 사유를 미리 조회한다. 위반이 있어도 예외를 던지지 않고 200으로 사유 목록을 반환한다.
     */
    @Override
    public ShopVisitGuideValidationResult validateVisitGuide(Long ceoId, Long shopId, String visitGuide) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<String> violations = shopRiderGuideValidator.findViolations(shop, visitGuide);
        return new ShopVisitGuideValidationResult(violations.isEmpty(), violations);
    }
}
