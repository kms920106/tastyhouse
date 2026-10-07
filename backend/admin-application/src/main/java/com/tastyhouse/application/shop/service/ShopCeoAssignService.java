package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopCeoAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopCeoAssignUseCase;

@Service
@Transactional
class ShopCeoAssignService implements ShopCeoAssignUseCase {

    private final ShopCeoAssignmentService shopCeoAssignmentService;

    public ShopCeoAssignService(ShopCeoAssignmentService shopCeoAssignmentService) {
        this.shopCeoAssignmentService = shopCeoAssignmentService;
    }

    @Override
    public void assignCeo(ShopCeoAssignCommand command) {
        Long adminId = command.adminId();
        Long id = command.shopId();
        Long ceoId = command.ceoId();

        ShopId shopId = ShopId.of(id);
        CeoId targetCeoId = CeoId.of(ceoId);
        shopCeoAssignmentService.assign(shopId, targetCeoId, adminId);
    }
}
