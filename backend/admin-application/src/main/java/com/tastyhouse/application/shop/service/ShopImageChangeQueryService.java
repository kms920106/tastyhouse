package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;
import com.tastyhouse.application.shop.port.in.ShopImageChangeQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopImageChangeRequestResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopImageChangeQueryService implements ShopImageChangeQueryUseCase {

    private final ShopManagementQueryPort shopManagementQueryPort;

    public ShopImageChangeQueryService(ShopManagementQueryPort shopManagementQueryPort) {
        this.shopManagementQueryPort = shopManagementQueryPort;
    }

    @Override
    public PageResult<ShopImageChangeRequestResult> getImageChangeRequests(
        String status,
        String imageType,
        int page,
        int size
    ) {
        String approvalStatus = status == null ? null : ApprovalStatus.valueOf(status).name();
        String type = imageType == null ? null : ShopImageType.from(imageType).name();

        return shopManagementQueryPort.findImageChangeRequestPage(approvalStatus, type, PageQuery.of(page, size));
    }
}
