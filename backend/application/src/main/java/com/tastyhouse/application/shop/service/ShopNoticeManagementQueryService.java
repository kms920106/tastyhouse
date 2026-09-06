package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopNoticeManagementListItemResult;
import com.tastyhouse.application.shop.port.out.ShopNoticeManagementQueryPort;
import com.tastyhouse.application.shop.port.in.ShopNoticeManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ShopNoticeManagementQueryService implements ShopNoticeManagementQueryUseCase {

    private final ShopNoticeManagementQueryPort shopNoticeManagementQueryPort;

    public ShopNoticeManagementQueryService(ShopNoticeManagementQueryPort shopNoticeManagementQueryPort) {
        this.shopNoticeManagementQueryPort = shopNoticeManagementQueryPort;
    }

    @Override
    public PageResult<ShopNoticeManagementListItemResult> getNotices(
        Long shopId,
        String shopName,
        Boolean hidden,
        int page,
        int size
    ) {
        return shopNoticeManagementQueryPort.findNoticePage(shopId, shopName, hidden, PageQuery.of(page, size));
    }
}
