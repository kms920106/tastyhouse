package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopOwnerListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopListItemResult;
import com.tastyhouse.application.shop.port.out.ShopSearchCondition;
import com.tastyhouse.application.shop.port.out.ShopSearchManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopOwnerListQueryService implements ShopOwnerListQueryUseCase {

    private final ShopSearchManagementQueryPort shopSearchManagementQueryPort;

    public ShopOwnerListQueryService(ShopSearchManagementQueryPort shopSearchManagementQueryPort) {
        this.shopSearchManagementQueryPort = shopSearchManagementQueryPort;
    }

    @Override
    public PageResult<ShopListItemResult> getMyShops(
        Long ceoId,
        String name,
        Long stationId,
        Boolean permanentlyClosed,
        int page,
        int size
    ) {
        ShopSearchCondition condition = ShopSearchCondition.of(name, stationId, permanentlyClosed, ceoId);
        return shopSearchManagementQueryPort.findShops(condition, PageQuery.of(page, size));
    }
}
