package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopBookmarkStatusQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopQueryPort;

@Service
@Transactional(readOnly = true)
class ShopBookmarkStatusQueryService implements ShopBookmarkStatusQueryUseCase {

    private final ShopQueryPort shopQueryPort;

    public ShopBookmarkStatusQueryService(ShopQueryPort shopQueryPort) {
        this.shopQueryPort = shopQueryPort;
    }

    @Override
    public boolean isBookmarked(Long shopId, Long memberId) {
        return shopQueryPort.existsBookmark(shopId, memberId);
    }
}
