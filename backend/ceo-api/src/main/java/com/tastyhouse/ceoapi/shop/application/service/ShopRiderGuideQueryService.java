package com.tastyhouse.ceoapi.shop.application.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.service.ShopRiderGuideValidator;
import com.tastyhouse.ceoapi.shop.ShopOwnershipValidator;
import com.tastyhouse.infrastructure.shop.query.ShopRiderGuideQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopRiderGuideResult;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRiderGuideResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRiderPickupLocationResponse;
import com.tastyhouse.ceoapi.shop.adapter.in.web.response.ShopRiderVisitGuideValidationResponse;

/**
 * 점주용 라이더 가게방문 안내 조회 서비스(CQRS query 측).
 *
 * <p>아직 한 번도 등록하지 않은 가게도 정상 응답을 돌려준다 — "미등록"은 오류가 아니라 정상 상태이므로
 * 문구·픽업 위치를 null로 채워 내려준다.
 */
@Service
@Transactional(readOnly = true)
public class ShopRiderGuideQueryService {

    private final ShopRiderGuideQueryDao shopRiderGuideQueryDao;
    private final ShopRiderGuideValidator shopRiderGuideValidator;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopRiderGuideQueryService(
        ShopRiderGuideQueryDao shopRiderGuideQueryDao,
        ShopRiderGuideValidator shopRiderGuideValidator,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopRiderGuideQueryDao = shopRiderGuideQueryDao;
        this.shopRiderGuideValidator = shopRiderGuideValidator;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public ShopRiderGuideResponse getRiderGuide(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        ShopRiderGuideResult result = shopRiderGuideQueryDao.findRiderGuide(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        return toShopRiderGuideResponse(result);
    }

    /**
     * 저장 전 위반 사유를 미리 조회한다. 위반이 있어도 예외를 던지지 않고 200으로 사유 목록을 반환한다.
     */
    public ShopRiderVisitGuideValidationResponse validateVisitGuide(Long ceoId, Long shopId, String visitGuide) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);

        List<String> violations = shopRiderGuideValidator.findViolations(shop, visitGuide);
        return ShopRiderVisitGuideValidationResponse.from(violations.isEmpty(), violations);
    }

    private ShopRiderGuideResponse toShopRiderGuideResponse(ShopRiderGuideResult result) {
        return ShopRiderGuideResponse.from(
            result.visitGuide(),
            toShopRiderPickupLocationResponse(result),
            result.shopRoadAddress(),
            result.shopLotAddress(),
            result.shopLatitude(),
            result.shopLongitude(),
            result.updatedAt()
        );
    }

    /**
     * 픽업 위치가 미설정이면 null을 반환해, 프론트가 "가게 실주소로 폴백" 상태임을 한 필드로 판정하게 한다.
     */
    private ShopRiderPickupLocationResponse toShopRiderPickupLocationResponse(ShopRiderGuideResult result) {
        String roadAddress = result.pickupRoadAddress();
        BigDecimal latitude = result.pickupLatitude();
        BigDecimal longitude = result.pickupLongitude();

        if (roadAddress == null || latitude == null || longitude == null) {
            return null;
        }

        return ShopRiderPickupLocationResponse.from(
            roadAddress,
            result.pickupLotAddress(),
            result.pickupDetailAddress(),
            latitude,
            longitude
        );
    }
}
