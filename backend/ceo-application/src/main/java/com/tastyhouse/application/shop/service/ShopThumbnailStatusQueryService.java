package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.application.shop.port.in.ShopThumbnailStatusQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopImageChangeRequestResult;
import com.tastyhouse.application.shop.port.out.ShopImageStatusResult;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopMediaOwnerQueryPort;

@Service
@Transactional(readOnly = true)
class ShopThumbnailStatusQueryService implements ShopThumbnailStatusQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopMediaOwnerQueryPort shopMediaOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopThumbnailStatusQueryService(ShopBasicInfoQueryPort shopBasicInfoQueryPort, ShopMediaOwnerQueryPort shopMediaOwnerQueryPort, ShopOwnershipValidator shopOwnershipValidator) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopMediaOwnerQueryPort = shopMediaOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public ShopImageStatusResult getThumbnailStatus(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        String thumbnailImageUrl = shopBasicInfoQueryPort.findShopImageUrls(shopId)
            .map(ShopImageUrlsResult::thumbnailImageUrl)
            .orElse(null);
        List<ShopImageChangeRequestResult> requests =
            shopMediaOwnerQueryPort.findImageChangeRequests(shopId, ShopImageType.THUMBNAIL.name());
        return new ShopImageStatusResult(thumbnailImageUrl, requests);
    }
}
