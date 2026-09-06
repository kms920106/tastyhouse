package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopTrademarkQueryUseCase;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.application.shop.port.out.ShopImageChangeRequestResult;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopImageStatusResult;

@Service
@CeoApp
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
