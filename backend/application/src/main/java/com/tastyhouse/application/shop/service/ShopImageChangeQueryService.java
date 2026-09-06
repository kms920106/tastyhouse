package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.shop.port.out.ShopImageChangeRequestResult;
import com.tastyhouse.application.shop.port.out.ShopManagementQueryPort;
import com.tastyhouse.application.shop.port.in.ShopImageChangeQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class ShopImageChangeQueryService implements ShopImageChangeQueryUseCase {

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
        ApprovalStatus approvalStatus = status == null ? null : ApprovalStatus.valueOf(status);
        ShopImageType type = imageType == null ? null : ShopImageType.from(imageType);

        return shopManagementQueryPort.findImageChangeRequestPage(approvalStatus, type, PageQuery.of(page, size));
    }
}
