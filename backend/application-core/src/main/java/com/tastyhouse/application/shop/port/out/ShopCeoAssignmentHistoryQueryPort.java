package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ShopCeoAssignmentHistoryQueryPort {

    PageResult<ShopCeoAssignmentHistoryResult> findShopAccessHistoryPage(ShopCeoAssignmentHistorySearchCondition condition, PageQuery pageQuery);
}
