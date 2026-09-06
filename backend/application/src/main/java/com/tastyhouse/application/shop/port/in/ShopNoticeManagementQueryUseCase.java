package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shop.port.out.ShopNoticeManagementListItemResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface ShopNoticeManagementQueryUseCase {

    PageResult<ShopNoticeManagementListItemResult> getNotices(
        Long shopId,
        String shopName,
        Boolean hidden,
        int page,
        int size
    );
}
