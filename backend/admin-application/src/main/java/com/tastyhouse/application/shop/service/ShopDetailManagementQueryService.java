package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopDetailManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopBasicInfoQueryPort;
import com.tastyhouse.application.shop.port.out.ShopImageUrlsResult;
import com.tastyhouse.application.shop.port.out.ShopManagementDetailResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopDetailManagementQueryService implements ShopDetailManagementQueryUseCase {

    private final ShopBasicInfoQueryPort shopBasicInfoQueryPort;
    private final ShopManagementQueryPort shopManagementQueryPort;

    public ShopDetailManagementQueryService(
        ShopBasicInfoQueryPort shopBasicInfoQueryPort,
        ShopManagementQueryPort shopManagementQueryPort
    ) {
        this.shopBasicInfoQueryPort = shopBasicInfoQueryPort;
        this.shopManagementQueryPort = shopManagementQueryPort;
    }

    @Override
    public ShopDetail getShop(Long id) {
        ShopManagementDetailResult shop = shopManagementQueryPort.findManagementDetailById(id)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));

        String thumbnailImageUrl = shopBasicInfoQueryPort.findShopImageUrls(shop.id())
            .map(ShopImageUrlsResult::thumbnailImageUrl)
            .orElse(null);

        return new ShopDetail(shop, thumbnailImageUrl);
    }
}
