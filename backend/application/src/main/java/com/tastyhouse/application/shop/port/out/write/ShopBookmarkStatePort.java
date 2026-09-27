package com.tastyhouse.application.shop.port.out.write;

public interface ShopBookmarkStatePort {
    boolean existsByShopIdAndMemberId(Long shopId, Long memberId);

    void deleteByShopIdAndMemberId(Long shopId, Long memberId);

    ShopBookmarkState save(ShopBookmarkState shopBookmark);
}
