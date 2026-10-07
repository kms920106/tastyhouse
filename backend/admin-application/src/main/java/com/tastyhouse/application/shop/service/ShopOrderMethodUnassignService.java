package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodUnassignCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodUnassignUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailPersistencePort;

@Service
@Transactional
class ShopOrderMethodUnassignService implements ShopOrderMethodUnassignUseCase {

    private final ShopDetailPersistencePort shopDetailPersistencePort;

    public ShopOrderMethodUnassignService(ShopDetailPersistencePort shopDetailPersistencePort) {
        this.shopDetailPersistencePort = shopDetailPersistencePort;
    }

    @Override
    public void unassignOrderMethod(ShopOrderMethodUnassignCommand command) {
        Long id = command.shopId();
        String orderMethod = command.orderMethod();

        shopDetailPersistencePort.deleteOrderMethodByShopIdAndOrderMethod(id, OrderMethod.from(orderMethod));
    }
}
