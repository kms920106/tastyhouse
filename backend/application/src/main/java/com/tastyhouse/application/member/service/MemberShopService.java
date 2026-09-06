package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;
import com.tastyhouse.application.shop.port.out.ShopSearchQueryPort;

@Service
@WebApp
public class MemberShopService {

    private final ShopSearchQueryPort shopSearchQueryPort;

    public MemberShopService(ShopSearchQueryPort shopSearchQueryPort) {
        this.shopSearchQueryPort = shopSearchQueryPort;
    }

    @Transactional(readOnly = true)
    public PageResult<ShopBookmarkedItemResult> getMyBookmarkedShops(Long memberId, int page, int size) {
        return shopSearchQueryPort.findMyBookmarkedShops(memberId, PageQuery.of(page, size));
    }
}
