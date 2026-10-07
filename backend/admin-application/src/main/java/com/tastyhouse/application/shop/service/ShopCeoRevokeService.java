package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopCeoRevokeCommand;
import com.tastyhouse.application.shop.port.in.ShopCeoRevokeUseCase;

@Service
@Transactional
class ShopCeoRevokeService implements ShopCeoRevokeUseCase {

    private final ShopCeoAssignmentService shopCeoAssignmentService;

    public ShopCeoRevokeService(ShopCeoAssignmentService shopCeoAssignmentService) {
        this.shopCeoAssignmentService = shopCeoAssignmentService;
    }

    @Override
    public void revokeCeo(ShopCeoRevokeCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();

        ShopId shopId = ShopId.of(id);
        shopCeoAssignmentService.revoke(shopId, adminId);
    }
}
