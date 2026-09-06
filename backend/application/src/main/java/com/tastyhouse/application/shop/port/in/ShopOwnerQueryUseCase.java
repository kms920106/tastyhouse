package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.application.shop.port.out.ShopOwnerDetailViewResult;

@CeoApp
public interface ShopOwnerQueryUseCase {

    PageResult<ShopListItemResult> getMyShops(
        Long ceoId,
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    );

    ShopOwnerDetailViewResult getMyShop(Long ceoId, Long shopId);
}
