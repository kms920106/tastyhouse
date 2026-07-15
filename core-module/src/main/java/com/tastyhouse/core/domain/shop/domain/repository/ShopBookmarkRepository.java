package com.tastyhouse.core.domain.shop.domain.repository;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public interface ShopBookmarkRepository {

    boolean existsByShopIdAndMemberId(Long shopId, MemberId memberId);

    void deleteByShopIdAndMemberId(Long shopId, MemberId memberId);
}
