package com.tastyhouse.application.search.port.in;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopBookmarkedItemResult;

public interface SearchShopPublicQueryUseCase {

    PageResult<ShopBookmarkedItemResult> searchShopsPublic(String query, int page, int size);
}
