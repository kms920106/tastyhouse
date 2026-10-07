package com.tastyhouse.application.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.member.port.in.MemberMyBookmarkedShopListQueryUseCase;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;

@Service
@Transactional(readOnly = true)
class MemberMyBookmarkedShopListQueryService implements MemberMyBookmarkedShopListQueryUseCase {

    private final MemberShopService memberShopService;

    public MemberMyBookmarkedShopListQueryService(MemberShopService memberShopService) {
        this.memberShopService = memberShopService;
    }

    @Override
    public PageResult<ShopBookmarkedItemResult> getMyBookmarkedShops(Long memberId, int page, int size) {
        return memberShopService.getMyBookmarkedShops(memberId, page, size);
    }
}
