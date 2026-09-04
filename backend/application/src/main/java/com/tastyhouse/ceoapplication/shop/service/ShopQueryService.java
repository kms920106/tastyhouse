package com.tastyhouse.ceoapplication.shop.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.ceoapplication.shop.port.in.ShopQueryUseCase;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopSearchCondition;
import com.tastyhouse.application.shop.port.out.ShopSearchManagementQueryPort;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopDetailViewResult;

/**
 * 점주용 가게 조회 서비스(CQRS query 측).
 *
 * <p>목록은 infra query DAO에서 Result를 받아 Response로 조립하고, 단건 상세는 소유권 검증이 반환한
 * 도메인 모델을 그대로 쓴다. 모든 조회는 로그인 점주(ceoId)의 소유 가게로 한정한다.
 */
@Service
@Transactional(readOnly = true)
public class ShopQueryService implements ShopQueryUseCase {

    private final ShopSearchManagementQueryPort shopSearchManagementQueryPort;
    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopQueryService(
        ShopSearchManagementQueryPort shopSearchManagementQueryPort,
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopSearchManagementQueryPort = shopSearchManagementQueryPort;
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public PageResult<ShopListItemResult> getMyShops(
        Long ceoId,
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    ) {
        ShopSearchCondition condition = ShopSearchCondition.of(name, stationId, permanentlyClosed, ceoId);
        return shopSearchManagementQueryPort.findShops(condition, PageQuery.of(page, size));
    }

    @Override
    public ShopDetailViewResult getMyShop(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toShopDetailViewResult(shop);
    }


    private ShopDetailViewResult toShopDetailViewResult(Shop shop) {
        Optional<ShopImageUrlsResult> imageUrls = shopBasicInfoQueryPort.findShopImageUrls(shop.getId());
        String thumbnailImageUrl = imageUrls.map(ShopImageUrlsResult::thumbnailImageUrl).orElse(null);
        String trademarkImageUrl = imageUrls.map(ShopImageUrlsResult::trademarkImageUrl).orElse(null);

        return new ShopDetailViewResult(
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
            shop.getMinOrderAmount(),
            shop.isScheduledOrderEnabled(),
            shop.isCupDepositEnabled()
        );
    }
}
