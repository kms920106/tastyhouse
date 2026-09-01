package com.tastyhouse.ceoapplication.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopTrademarkQueryUseCase;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.application.shop.port.out.ShopImageChangeRequestResult;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopImageStatusResult;

/**
 * 점주용 가게 상표/대표이미지 상태 조회 서비스(CQRS query 측).
 *
 * <p>현재 적용된 이미지 URL은 소유권 검증이 반환한 도메인 모델에서, 진행 중·과거 변경요청 목록은
 * infra query DAO에서 얻어 함께 조립한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopTrademarkQueryService implements ShopTrademarkQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnerQueryPort shopOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopTrademarkQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopOwnerQueryPort shopOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnerQueryPort = shopOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopImageStatusResult getTrademarkStatus(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String trademarkImageUrl = shopBasicInfoQueryPort.findShopImageUrls(shopId)
            .map(ShopImageUrlsResult::trademarkImageUrl)
            .orElse(null);
        return toShopImageStatusResult(trademarkImageUrl, shopId, ShopImageType.TRADEMARK);
    }

    @Override
    public ShopImageStatusResult getThumbnailStatus(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String thumbnailImageUrl = shopBasicInfoQueryPort.findShopImageUrls(shopId)
            .map(ShopImageUrlsResult::thumbnailImageUrl)
            .orElse(null);
        return toShopImageStatusResult(thumbnailImageUrl, shopId, ShopImageType.THUMBNAIL);
    }

    private ShopImageStatusResult toShopImageStatusResult(
        String currentImageUrl,
        Long shopId,
        ShopImageType imageType
    ) {
        List<ShopImageChangeRequestResult> requests =
            shopOwnerQueryPort.findImageChangeRequests(shopId, imageType);
        return new ShopImageStatusResult(currentImageUrl, requests);
    }
}
