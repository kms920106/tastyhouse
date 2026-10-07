package com.tastyhouse.application.shop.port.in;

import java.util.List;

import com.tastyhouse.application.shop.port.out.ShopRiderGuideHistoryResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;

public interface ShopRiderGuideManagementDetailQueryUseCase {

    ShopRiderGuideDetail getRiderGuide(Long shopId);

    record ShopRiderGuideDetail(
        ShopRiderGuideResult guide,
        List<ShopRiderGuideHistoryResult> histories
    ) {
    }
}
