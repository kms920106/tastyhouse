package com.tastyhouse.core.domain.shop.domain.repository;

import com.tastyhouse.core.domain.shop.domain.model.ShopBookmark;

import java.util.Optional;

public interface ShopBookmarkRepository {

    Optional<ShopBookmark> findByShopIdAndMemberId(Long shopId, Long memberId);

    boolean existsByShopIdAndMemberId(Long shopId, Long memberId);

    void deleteByShopIdAndMemberId(Long shopId, Long memberId);
}
