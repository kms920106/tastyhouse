package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopOrderNoticeOwnerQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopOrderNoticeResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopOrderNoticeOwnerQueryService implements ShopOrderNoticeOwnerQueryUseCase {

    private final ShopOrderNoticeManagementQueryPort shopOrderNoticeManagementQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOrderNoticeOwnerQueryService(
        ShopOrderNoticeManagementQueryPort shopOrderNoticeManagementQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopOrderNoticeManagementQueryPort = shopOrderNoticeManagementQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public Optional<ShopOrderNoticeResult> getOrderNotice(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopOrderNoticeManagementQueryPort.findOrderNotice(shopId);
    }
}
