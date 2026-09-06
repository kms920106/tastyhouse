package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface ShopContentBoardManagementQueryUseCase {

    PageResult<ShopContentBoardResult> getContentBoards(
        Long shopId,
        Boolean hidden,
        String contentType,
        int page,
        int size
    );
}
