package com.tastyhouse.application.shop.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.application.shop.port.in.ShopOwnerDetailQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopOwnerDetailViewResult;

@Service
@Transactional(readOnly = true)
class ShopOwnerDetailQueryService implements ShopOwnerDetailQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOwnerDetailQueryService(
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
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
