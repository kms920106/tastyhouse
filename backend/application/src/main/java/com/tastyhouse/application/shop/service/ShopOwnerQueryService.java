package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopOwnerQueryUseCase;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopSearchCondition;
import com.tastyhouse.application.shop.port.out.ShopSearchManagementQueryPort;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopOwnerDetailViewResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopOwnerQueryService implements ShopOwnerQueryUseCase {

    private final ShopSearchManagementQueryPort shopSearchManagementQueryPort;
    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOwnerQueryService(
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
    public ShopOwnerDetailViewResult getMyShop(Long ceoId, Long shopId) {
        Shop shop = shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return toShopDetailViewResult(shop);
    }

    private ShopOwnerDetailViewResult toShopDetailViewResult(Shop shop) {
        Optional<ShopImageUrlsResult> imageUrls = shopBasicInfoQueryPort.findShopImageUrls(shop.getId());
        String thumbnailImageUrl = imageUrls.map(ShopImageUrlsResult::thumbnailImageUrl).orElse(null);
        String trademarkImageUrl = imageUrls.map(ShopImageUrlsResult::trademarkImageUrl).orElse(null);

        return new ShopOwnerDetailViewResult(
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
