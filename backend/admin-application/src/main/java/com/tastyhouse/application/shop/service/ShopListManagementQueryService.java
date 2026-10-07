package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopListManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.application.shop.port.out.ShopSearchCondition;
import com.tastyhouse.application.shop.port.out.ShopSearchManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopListManagementQueryService implements ShopListManagementQueryUseCase {

    private final ShopSearchManagementQueryPort shopSearchManagementQueryPort;

    public ShopListManagementQueryService(ShopSearchManagementQueryPort shopSearchManagementQueryPort) {
        this.shopSearchManagementQueryPort = shopSearchManagementQueryPort;
    }

    @Override
    public PageResult<ShopListItemResult> getShops(
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    ) {
        ShopSearchCondition condition = ShopSearchCondition.of(name, stationId, permanentlyClosed);
        return shopSearchManagementQueryPort.findShops(condition, PageQuery.of(page, size));
    }
}
