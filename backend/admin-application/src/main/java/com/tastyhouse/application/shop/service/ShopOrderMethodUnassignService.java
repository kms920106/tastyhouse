package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodUnassignUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopOrderMethodSavePort;

@Service
@Transactional
class ShopOrderMethodUnassignService implements ShopOrderMethodUnassignUseCase {

    private final ShopOrderMethodSavePort shopOrderMethodSavePort;

    public ShopOrderMethodUnassignService(ShopOrderMethodSavePort shopOrderMethodSavePort) {
        this.shopOrderMethodSavePort = shopOrderMethodSavePort;
    }

    @Override
    public void unassignOrderMethod(ShopOrderMethodUnassignCommand command) {
        Long id = command.shopId();
        String orderMethod = command.orderMethod();

        shopOrderMethodSavePort.deleteOrderMethodByShopIdAndOrderMethod(id, OrderMethod.from(orderMethod));
    }
}
