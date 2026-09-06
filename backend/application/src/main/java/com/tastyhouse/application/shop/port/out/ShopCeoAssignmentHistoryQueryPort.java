package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface ShopCeoAssignmentHistoryQueryPort {

    PageResult<ShopCeoAssignmentHistoryResult> findShopAccessHistoryPage(ShopCeoAssignmentHistorySearchCondition condition, PageQuery pageQuery);
}
