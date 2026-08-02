package com.tastyhouse.domain.shop.repository;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopBookmark;

public interface ShopBookmarkRepository {

    boolean existsByShopIdAndMemberId(Long shopId, MemberId memberId);

    void deleteByShopIdAndMemberId(Long shopId, MemberId memberId);

    ShopBookmark save(ShopBookmark shopBookmark);
}
