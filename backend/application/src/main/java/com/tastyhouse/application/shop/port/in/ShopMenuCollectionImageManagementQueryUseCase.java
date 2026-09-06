package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageRequestResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface ShopMenuCollectionImageManagementQueryUseCase {

    PageResult<ShopMenuCollectionImageRequestResult> getMenuCollectionImageRequests(
        String status,
        int page,
        int size
    );
}
