package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopMenuCollectionImageRequestResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.application.shop.port.in.ShopMenuCollectionImageManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ShopMenuCollectionImageManagementQueryService implements ShopMenuCollectionImageManagementQueryUseCase {

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
        ApprovalStatus approvalStatus = promoteStatus(status);

        return shopManagementQueryPort.findMenuCollectionImageRequestPage(approvalStatus, PageQuery.of(page, size));
    }

    private ApprovalStatus promoteStatus(String status) {
        return status == null ? null : ApprovalStatus.valueOf(status);
    }
}
