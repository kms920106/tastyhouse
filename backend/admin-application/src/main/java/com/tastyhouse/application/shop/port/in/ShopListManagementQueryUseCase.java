package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;

public interface ShopListManagementQueryUseCase {

    PageResult<ShopListItemResult> getShops(
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    );
}
