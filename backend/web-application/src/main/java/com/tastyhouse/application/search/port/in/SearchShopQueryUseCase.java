package com.tastyhouse.application.search.port.in;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;

public interface SearchShopQueryUseCase {

    PageResult<ShopBookmarkedItemResult> searchShopsPaged(String query, Long memberId, int page, int size);
}
