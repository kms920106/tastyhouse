package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageRequestResult;

@Service
@Transactional(readOnly = true)
class ShopMenuCollectionImageManagementQueryService implements ShopMenuCollectionImageManagementQueryUseCase {

    private final ShopManagementQueryPort shopManagementQueryPort;

    public ShopMenuCollectionImageManagementQueryService(ShopManagementQueryPort shopManagementQueryPort) {
        this.shopManagementQueryPort = shopManagementQueryPort;
    }

    @Override
    public PageResult<ShopMenuCollectionImageRequestResult> getMenuCollectionImageRequests(
        String status,
        int page,
        int size
    ) {
        String approvalStatus = promoteStatus(status);

        return shopManagementQueryPort.findMenuCollectionImageRequestPage(approvalStatus, PageQuery.of(page, size));
    }

    private String promoteStatus(String status) {
        return status == null ? null : ApprovalStatus.valueOf(status).name();
    }
}
