package com.tastyhouse.adminapi.shop;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.query.ShopRiderGuideHistoryResult;
import com.tastyhouse.infrastructure.shop.query.ShopRiderGuideListItemResult;
import com.tastyhouse.infrastructure.shop.query.ShopRiderGuideQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopRiderGuideResult;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.shop.response.ShopRiderGuideDetailResponse;
import com.tastyhouse.adminapi.shop.response.ShopRiderGuideHistoryResponse;
import com.tastyhouse.adminapi.shop.response.ShopRiderGuideListItemResponse;
import com.tastyhouse.adminapi.shop.response.ShopRiderPickupLocationResponse;

/**
 * admin용 라이더 안내 검수 조회 서비스(CQRS query 측).
 *
 * <p>소유권 검증 없이 전체 가게의 라이더 안내를 조회한다(admin 무제한 원칙).
 */
@Service
@Transactional(readOnly = true)
public class ShopRiderGuideQueryService {

    private final ShopRiderGuideQueryDao shopRiderGuideQueryDao;

    public ShopRiderGuideQueryService(ShopRiderGuideQueryDao shopRiderGuideQueryDao) {
        this.shopRiderGuideQueryDao = shopRiderGuideQueryDao;
    }

    public PaginationResponse<ShopRiderGuideListItemResponse> getRiderGuides(
        String shopName,
        Boolean hasVisitGuide,
        int page,
        int size
    ) {
        PageResult<ShopRiderGuideListItemResult> pageResult = shopRiderGuideQueryDao
            .findRiderGuidePage(shopName, hasVisitGuide, PageQuery.of(page, size));

        return PaginationResponse.from(pageResult.map(this::toShopRiderGuideListItemResponse));
    }

    public ShopRiderGuideDetailResponse getRiderGuide(Long shopId) {
        ShopRiderGuideResult result = shopRiderGuideQueryDao.findRiderGuide(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        List<ShopRiderGuideHistoryResponse> histories = shopRiderGuideQueryDao.findHistories(shopId).stream()
            .map(this::toShopRiderGuideHistoryResponse)
            .toList();

        return ShopRiderGuideDetailResponse.from(
            result.shopId(),
            result.shopName(),
            result.shopRoadAddress(),
            result.visitGuide(),
            toShopRiderPickupLocationResponse(result),
            histories
        );
    }

    private ShopRiderGuideListItemResponse toShopRiderGuideListItemResponse(ShopRiderGuideListItemResult dto) {
        return ShopRiderGuideListItemResponse.from(
            dto.shopId(),
            dto.shopName(),
            dto.visitGuide(),
            dto.hasPickupLocation(),
            dto.updatedAt()
        );
    }

    private ShopRiderGuideHistoryResponse toShopRiderGuideHistoryResponse(ShopRiderGuideHistoryResult dto) {
        return ShopRiderGuideHistoryResponse.from(
            dto.id(),
            dto.actorType().name(),
            dto.actorId(),
            dto.actionType().name(),
            dto.previousVisitGuide(),
            dto.newVisitGuide(),
            dto.reason(),
            dto.createdAt()
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
