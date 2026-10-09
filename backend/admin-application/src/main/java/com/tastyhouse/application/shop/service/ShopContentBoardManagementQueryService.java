package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopContentBoardManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopContentBoardResult;
import com.tastyhouse.application.shop.port.out.ShopMediaManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopContentBoardManagementQueryService implements ShopContentBoardManagementQueryUseCase {

    private final ShopMediaManagementQueryPort shopMediaManagementQueryPort;

    public ShopContentBoardManagementQueryService(ShopMediaManagementQueryPort shopMediaManagementQueryPort) {
        this.shopMediaManagementQueryPort = shopMediaManagementQueryPort;
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

        return shopMediaManagementQueryPort.findContentBoardPage(shopId, hidden, type, PageQuery.of(page, size));
    }
}
