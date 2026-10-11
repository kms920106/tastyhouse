package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopBookmark;

public interface ShopBookmarkSavePort {

    void deleteByShopIdAndMemberId(Long shopId, MemberId memberId);

    ShopBookmark save(ShopBookmark shopBookmark);
}
