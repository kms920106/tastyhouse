package com.tastyhouse.application.member.port.in;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;

public interface MemberMyBookmarkedShopListQueryUseCase {

    PageResult<ShopBookmarkedItemResult> getMyBookmarkedShops(Long memberId, int page, int size);
}
