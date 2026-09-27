package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.application.shop.port.in.ShopContentBoardManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ShopContentBoardManagementQueryService implements ShopContentBoardManagementQueryUseCase {

    private final ShopManagementQueryPort shopManagementQueryPort;

    public ShopContentBoardManagementQueryService(ShopManagementQueryPort shopManagementQueryPort) {
        this.shopManagementQueryPort = shopManagementQueryPort;
    }

    @Override
    public PageResult<ShopContentBoardResult> getContentBoards(
        Long shopId,
        Boolean hidden,
        String contentType,
        int page,
        int size
    ) {
        String type = contentType == null ? null : ShopContentType.from(contentType).name();

        return shopManagementQueryPort.findContentBoardPage(shopId, hidden, type, PageQuery.of(page, size));
    }
}
