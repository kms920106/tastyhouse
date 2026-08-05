package com.tastyhouse.ceoapi.shop;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.shop.query.ShopImageUrlsResult;
import com.tastyhouse.infrastructure.shop.query.ShopListItemResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.infrastructure.shop.query.ShopSearchCondition;
import com.tastyhouse.infrastructure.shop.query.ShopSearchQueryDao;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.ceoapi.shop.response.ShopDetailResponse;
import com.tastyhouse.ceoapi.shop.response.ShopListItemResponse;

/**
 * 점주용 가게 조회 서비스(CQRS query 측).
 *
 * <p>목록은 infra query DAO에서 Result를 받아 Response로 조립하고, 단건 상세는 소유권 검증이 반환한
 * 도메인 모델을 그대로 쓴다. 모든 조회는 로그인 점주(ceoId)의 소유 가게로 한정한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopQueryService {

    private final ShopSearchQueryDao shopSearchQueryDao;
    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopQueryService(
        ShopSearchQueryDao shopSearchQueryDao,
        ShopQueryDao shopQueryDao,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopSearchQueryDao = shopSearchQueryDao;
        this.shopQueryDao = shopQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public PaginationResponse<ShopListItemResponse> getMyShops(
        Long ceoId,
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    ) {
        ShopSearchCondition condition = ShopSearchCondition.of(name, stationId, permanentlyClosed, ceoId);
        PageResult<ShopListItemResponse> pageResult =
            shopSearchQueryDao.findShops(condition, PageQuery.of(page, size))
                .map(this::toShopListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public ShopDetailResponse getMyShop(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toShopDetailResponse(shop);
    }

    private ShopListItemResponse toShopListItemResponse(ShopListItemResult dto) {
        return ShopListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.stationName(),
            dto.roadAddress(),
            dto.rating(),
            dto.permanentlyClosed()
        );
    }

    private ShopDetailResponse toShopDetailResponse(Shop shop) {
        Optional<ShopImageUrlsResult> imageUrls = shopQueryDao.findShopImageUrls(shop.getId());
        String thumbnailImageUrl = imageUrls.map(ShopImageUrlsResult::thumbnailImageUrl).orElse(null);
        String trademarkImageUrl = imageUrls.map(ShopImageUrlsResult::trademarkImageUrl).orElse(null);

        return ShopDetailResponse.from(
            shop.getId(),
            shop.getStationId() == null ? null : shop.getStationId().value(),
            shop.getName(),
            shop.getLatitude(),
            shop.getLongitude(),
            shop.getRating(),
            shop.getRoadAddress(),
            shop.getLotAddress(),
            shop.getPhoneNumber(),
            thumbnailImageUrl,
            trademarkImageUrl,
            shop.isPermanentlyClosed(),
            shop.isHidden(),
            shop.isClosedOnPublicHolidays(),
            shop.getMinOrderAmount()
        );
    }
}
