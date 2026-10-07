package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shared.exception.AdminErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.in.ShopChoiceDetailManagementQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopChoiceDetailResult;
import com.tastyhouse.application.shop.port.out.ShopChoiceManagementQueryPort;

@Service
@Transactional(readOnly = true)
class ShopChoiceDetailManagementQueryService implements ShopChoiceDetailManagementQueryUseCase {

    private final ShopChoiceManagementQueryPort shopChoiceManagementQueryPort;

    public ShopChoiceDetailManagementQueryService(ShopChoiceManagementQueryPort shopChoiceManagementQueryPort) {
        this.shopChoiceManagementQueryPort = shopChoiceManagementQueryPort;
    }

    @Override
    public ShopChoiceDetailResult getShopChoice(Long id) {
        return shopChoiceManagementQueryPort.findShopChoiceById(id)
            .orElseThrow(() -> new ResourceNotFoundException(AdminErrorCode.SHOP_CHOICE_NOT_FOUND));
    }
}
