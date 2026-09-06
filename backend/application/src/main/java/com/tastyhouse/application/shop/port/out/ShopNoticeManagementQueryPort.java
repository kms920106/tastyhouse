package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface ShopNoticeManagementQueryPort {

    PageResult<ShopNoticeManagementListItemResult> findNoticePage(Long shopId, String shopName, Boolean hidden, PageQuery pageQuery);
}
