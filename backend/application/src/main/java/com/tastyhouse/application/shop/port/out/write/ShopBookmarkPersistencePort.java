package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopBookmark;

public interface ShopBookmarkPersistencePort {

    boolean existsByShopIdAndMemberId(Long shopId, MemberId memberId);

    void deleteByShopIdAndMemberId(Long shopId, MemberId memberId);

    ShopBookmark save(ShopBookmark shopBookmark);
}
