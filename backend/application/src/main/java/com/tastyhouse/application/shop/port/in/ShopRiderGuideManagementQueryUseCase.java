package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;

@AdminApp
public interface ShopRiderGuideManagementQueryUseCase {

    PageResult<ShopRiderGuideListItemResult> getRiderGuides(
        String shopName,
        Boolean hasVisitGuide,
        int page,
        int size
    );

    ShopRiderGuideDetail getRiderGuide(Long shopId);

    record ShopRiderGuideDetail(
        ShopRiderGuideResult guide,
        List<ShopRiderGuideHistoryResult> histories
    ) {
    }
}
