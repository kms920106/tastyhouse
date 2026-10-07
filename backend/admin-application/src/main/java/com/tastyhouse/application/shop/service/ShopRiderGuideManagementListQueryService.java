package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopRiderGuideManagementListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopRiderGuideManagementListQueryService implements ShopRiderGuideManagementListQueryUseCase {

    private final ShopRiderGuideManagementQueryPort shopRiderGuideManagementQueryPort;

    public ShopRiderGuideManagementListQueryService(ShopRiderGuideManagementQueryPort shopRiderGuideManagementQueryPort) {
        this.shopRiderGuideManagementQueryPort = shopRiderGuideManagementQueryPort;
    }

    @Override
    public PageResult<ShopRiderGuideListItemResult> getRiderGuides(
        String shopName,
        Boolean hasVisitGuide,
        int page,
        int size
    ) {
        return shopRiderGuideManagementQueryPort.findRiderGuidePage(shopName, hasVisitGuide, PageQuery.of(page, size));
    }
}
