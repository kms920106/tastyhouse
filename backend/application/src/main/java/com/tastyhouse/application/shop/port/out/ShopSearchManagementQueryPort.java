package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ShopSearchManagementQueryPort {

    PageResult<ShopListItemResult> findShops(ShopSearchCondition condition, PageQuery pageQuery);
}
