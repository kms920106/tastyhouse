package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.model.ShopOrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodAssignCommand;
import com.tastyhouse.application.shop.port.in.ShopOrderMethodAssignUseCase;
import com.tastyhouse.application.shop.port.out.write.ShopDetailSavePort;

@Service
@Transactional
class ShopOrderMethodAssignService implements ShopOrderMethodAssignUseCase {

    private final ShopDetailSavePort shopDetailSavePort;

    public ShopOrderMethodAssignService(ShopDetailSavePort shopDetailSavePort) {
        this.shopDetailSavePort = shopDetailSavePort;
    }

    @Override
    public Long assignOrderMethod(ShopOrderMethodAssignCommand command) {
        Long id = command.shopId();
        String orderMethod = command.orderMethod();

        ShopOrderMethod saved = shopDetailSavePort.saveOrderMethod(
            ShopOrderMethod.of(ShopId.of(id), OrderMethod.from(orderMethod))
        );
        return saved.getId();
    }
}
