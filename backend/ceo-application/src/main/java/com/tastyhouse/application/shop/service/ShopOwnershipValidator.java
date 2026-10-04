package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopPersistencePort;

@Component
public class ShopOwnershipValidator {

    private final ShopPersistencePort shopPersistencePort;

    public ShopOwnershipValidator(ShopPersistencePort shopPersistencePort) {
        this.shopPersistencePort = shopPersistencePort;
    }

    public Shop validateOwnership(Long ceoId, Long shopId) {
        Shop shop = shopPersistencePort.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        if (shop.getCeoId() == null || !shop.getCeoId().equals(CeoId.of(ceoId))) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }
        return shop;
    }
}
