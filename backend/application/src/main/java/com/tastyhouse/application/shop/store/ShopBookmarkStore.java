package com.tastyhouse.application.shop.store;

import com.tastyhouse.application.shop.port.out.write.ShopBookmarkStatePort;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopBookmark;

public class ShopBookmarkStore implements ShopBookmarkRepository {
    private final ShopBookmarkStatePort shopBookmarkStatePort;

    public ShopBookmarkStore(ShopBookmarkStatePort shopBookmarkStatePort) {
        this.shopBookmarkStatePort = shopBookmarkStatePort;
    }

    @Override
    public boolean existsByShopIdAndMemberId(Long shopId, MemberId memberId) {
        return shopBookmarkStatePort.existsByShopIdAndMemberId(shopId, memberId.value());
    }

    @Override
    public void deleteByShopIdAndMemberId(Long shopId, MemberId memberId) {
        shopBookmarkStatePort.deleteByShopIdAndMemberId(shopId, memberId.value());
    }

    @Override
    public ShopBookmark save(ShopBookmark shopBookmark) {
        return ShopBookmarkStateMapper.toDomain(shopBookmarkStatePort.save(ShopBookmarkStateMapper.toState(shopBookmark)));
    }
}
