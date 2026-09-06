package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shop.port.out.ShopImageChangeRequestResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface ShopImageChangeQueryUseCase {

    PageResult<ShopImageChangeRequestResult> getImageChangeRequests(
        String status,
        String imageType,
        int page,
        int size
    );
}
