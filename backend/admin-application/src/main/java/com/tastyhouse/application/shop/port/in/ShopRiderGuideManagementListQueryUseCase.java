package com.tastyhouse.application.shop.port.in;

import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideListItemResult;

public interface ShopRiderGuideManagementListQueryUseCase {

    PageResult<ShopRiderGuideListItemResult> getRiderGuides(
        String shopName,
        Boolean hasVisitGuide,
        int page,
        int size
    );
}
