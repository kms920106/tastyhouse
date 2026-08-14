package com.tastyhouse.ceoapi.shop;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.infrastructure.shop.query.ShopImageChangeRequestResult;
import com.tastyhouse.infrastructure.shop.query.ShopImageUrlsResult;
import com.tastyhouse.infrastructure.shop.query.ShopQueryDao;
import com.tastyhouse.ceoapi.shop.response.ShopImageChangeRequestItemResponse;
import com.tastyhouse.ceoapi.shop.response.ShopImageStatusResponse;

/**
 * 점주용 가게 상표/대표이미지 상태 조회 서비스(CQRS query 측).
 *
 * <p>현재 적용된 이미지 URL은 소유권 검증이 반환한 도메인 모델에서, 진행 중·과거 변경요청 목록은
 * infra query DAO에서 얻어 함께 조립한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopTrademarkQueryService {

    private final ShopQueryDao shopQueryDao;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopTrademarkQueryService(ShopQueryDao shopQueryDao, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopQueryDao = shopQueryDao;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    public ShopImageStatusResponse getTrademarkStatus(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String trademarkImageUrl = shopQueryDao.findShopImageUrls(shopId)
            .map(ShopImageUrlsResult::trademarkImageUrl)
            .orElse(null);
        return toShopImageStatusResponse(trademarkImageUrl, shopId, ShopImageType.TRADEMARK);
    }

    public ShopImageStatusResponse getThumbnailStatus(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String thumbnailImageUrl = shopQueryDao.findShopImageUrls(shopId)
            .map(ShopImageUrlsResult::thumbnailImageUrl)
            .orElse(null);
        return toShopImageStatusResponse(thumbnailImageUrl, shopId, ShopImageType.THUMBNAIL);
    }

    private ShopImageStatusResponse toShopImageStatusResponse(
        String currentImageUrl,
        Long shopId,
        ShopImageType imageType
    ) {
        List<ShopImageChangeRequestItemResponse> requests = shopQueryDao.findImageChangeRequests(shopId, imageType).stream()
            .map(this::toShopImageChangeRequestItemResponse)
            .toList();
        return ShopImageStatusResponse.of(currentImageUrl, requests);
    }

    private ShopImageChangeRequestItemResponse toShopImageChangeRequestItemResponse(ShopImageChangeRequestResult dto) {
        return ShopImageChangeRequestItemResponse.of(
            dto.id(),
            dto.imageType().name(),
            dto.imageUrl(),
            dto.status().name(),
            dto.rejectReason()
        );
    }

}
