package com.tastyhouse.core.domain.shop.domain.repository;

public interface ShopBookmarkRepository {

    boolean existsByShopIdAndMemberId(Long shopId, Long memberId);

    void deleteByShopIdAndMemberId(Long shopId, Long memberId);
}
