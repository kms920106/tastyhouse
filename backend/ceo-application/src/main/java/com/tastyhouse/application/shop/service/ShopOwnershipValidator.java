package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopLoadPort;

@Component
public class ShopOwnershipValidator {

    private final ShopLoadPort shopLoadPort;

    public ShopOwnershipValidator(ShopLoadPort shopLoadPort) {
        this.shopLoadPort = shopLoadPort;
    }

    public Shop validateOwnership(Long ceoId, Long shopId) {
        Shop shop = shopLoadPort.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOT_FOUND));
        if (shop.getCeoId() == null || !shop.getCeoId().equals(CeoId.of(ceoId))) {
            throw new ApplicationException(ApplicationErrorCode.SHOP_ACCESS_DENIED);
        }
        return shop;
    }
}
