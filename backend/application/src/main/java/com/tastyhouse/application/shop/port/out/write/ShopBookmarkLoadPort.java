package com.tastyhouse.application.shop.port.out.write;

import com.tastyhouse.domain.member.vo.MemberId;

public interface ShopBookmarkLoadPort {

    boolean existsByShopIdAndMemberId(Long shopId, MemberId memberId);
}
