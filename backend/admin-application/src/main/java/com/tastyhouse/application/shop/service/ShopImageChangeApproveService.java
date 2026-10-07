package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopImageChangeApproveCommand;
import com.tastyhouse.application.shop.port.in.ShopImageChangeApproveUseCase;

@Service
@Transactional
class ShopImageChangeApproveService implements ShopImageChangeApproveUseCase {

    private final ShopImageApprovalService shopImageApprovalService;

    public ShopImageChangeApproveService(ShopImageApprovalService shopImageApprovalService) {
        this.shopImageApprovalService = shopImageApprovalService;
    }

    @Override
    public void approveImageChange(ShopImageChangeApproveCommand command) {
        Long id = command.requestId();
        shopImageApprovalService.approveImageChange(id);
    }
}
