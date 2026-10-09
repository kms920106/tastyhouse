package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.member.port.in.MemberMyBookmarkedShopListQueryUseCase;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;

@Service
@Transactional(readOnly = true)
class MemberMyBookmarkedShopListQueryService implements MemberMyBookmarkedShopListQueryUseCase {

    private final ShopSearchQueryPort shopSearchQueryPort;

    public MemberMyBookmarkedShopListQueryService(ShopSearchQueryPort shopSearchQueryPort) {
        this.shopSearchQueryPort = shopSearchQueryPort;
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> getMyBookmarkedShops(Long memberId, int page, int size) {
        return shopSearchQueryPort.findMyBookmarkedShops(memberId, PageQuery.of(page, size));
    }
}
