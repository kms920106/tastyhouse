package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface ShopSearchManagementQueryPort {

    PageResult<ShopListItemResult> findShops(ShopSearchCondition condition, PageQuery pageQuery);
}
