package com.tastyhouse.application.shop.port.out;

import java.util.List;
import java.util.Optional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface ShopRiderGuideManagementQueryPort {

    Optional<ShopRiderGuideResult> findRiderGuide(Long shopId);

    PageResult<ShopRiderGuideListItemResult> findRiderGuidePage(String shopName, Boolean hasVisitGuide, PageQuery pageQuery);

    List<ShopRiderGuideHistoryResult> findHistories(Long shopId);
}
