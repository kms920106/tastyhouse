package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideListItemResult;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopRiderGuideResult;
import com.tastyhouse.application.shop.port.in.ShopRiderGuideManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ShopRiderGuideManagementQueryService implements ShopRiderGuideManagementQueryUseCase {

    private final ShopRiderGuideManagementQueryPort shopRiderGuideManagementQueryPort;

    public ShopRiderGuideManagementQueryService(ShopRiderGuideManagementQueryPort shopRiderGuideManagementQueryPort) {
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

    @Override
    public ShopRiderGuideDetail getRiderGuide(Long shopId) {
        ShopRiderGuideResult result = shopRiderGuideManagementQueryPort.findRiderGuide(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));

        return new ShopRiderGuideDetail(result, shopRiderGuideManagementQueryPort.findHistories(shopId));
    }
}
