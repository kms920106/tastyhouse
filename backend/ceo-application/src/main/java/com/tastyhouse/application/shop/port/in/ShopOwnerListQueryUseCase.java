package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;

public interface ShopOwnerListQueryUseCase {

    PageResult<ShopListItemResult> getMyShops(
        Long ceoId,
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    );
}
