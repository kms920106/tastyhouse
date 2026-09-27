package com.tastyhouse.application.shop.port.out;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface ShopNoticeManagementQueryPort {

    PageResult<ShopNoticeManagementListItemResult> findNoticePage(Long shopId, String shopName, Boolean hidden, PageQuery pageQuery);
}
