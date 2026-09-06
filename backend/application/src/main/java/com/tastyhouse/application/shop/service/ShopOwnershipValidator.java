package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Component;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.repository.ShopRepository;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Component
@CeoApp
public class ShopOwnershipValidator {

    private final ShopRepository shopRepository;

    public ShopOwnershipValidator(ShopRepository shopRepository) {
        this.shopRepository = shopRepository;
    }

    public Shop validateOwnership(Long ceoId, Long shopId) {
        Shop shop = shopRepository.findById(ShopId.of(shopId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_NOT_FOUND));
        if (shop.getCeoId() == null || !shop.getCeoId().equals(CeoId.of(ceoId))) {
            throw new BusinessException(ErrorCode.SHOP_ACCESS_DENIED);
        }
        return shop;
    }
}
