package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopImageChangeRejectCommand;
import com.tastyhouse.application.shop.port.in.ShopImageChangeRejectUseCase;

@Service
@Transactional
class ShopImageChangeRejectService implements ShopImageChangeRejectUseCase {

    private final ShopImageApprovalService shopImageApprovalService;

    public ShopImageChangeRejectService(ShopImageApprovalService shopImageApprovalService) {
        this.shopImageApprovalService = shopImageApprovalService;
    }

    @Override
    public void rejectImageChange(ShopImageChangeRejectCommand command) {
        Long id = command.requestId();
        String reason = command.reason();
        shopImageApprovalService.rejectImageChange(id, reason);
    }
}
