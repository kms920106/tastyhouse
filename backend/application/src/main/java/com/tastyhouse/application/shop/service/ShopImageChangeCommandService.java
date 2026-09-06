package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shop.port.in.ShopImageChangeApproveCommand;
import com.tastyhouse.application.shop.port.in.ShopImageChangeCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopImageChangeRejectCommand;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopImageApprovalService;

@Service
@AdminApp
@Transactional
public class ShopImageChangeCommandService implements ShopImageChangeCommandUseCase {

    private final ShopImageApprovalService shopImageApprovalService;

    public ShopImageChangeCommandService(ShopImageApprovalService shopImageApprovalService) {
        this.shopImageApprovalService = shopImageApprovalService;
    }

    @Override
    public void approveImageChange(ShopImageChangeApproveCommand command) {
        Long id = command.requestId();
        shopImageApprovalService.approveImageChange(id);
    }

    @Override
    public void rejectImageChange(ShopImageChangeRejectCommand command) {
        Long id = command.requestId();
        String reason = command.reason();
        shopImageApprovalService.rejectImageChange(id, reason);
    }
}
