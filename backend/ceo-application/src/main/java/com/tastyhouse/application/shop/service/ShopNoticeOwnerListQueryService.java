package com.tastyhouse.application.shop.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopNoticeOwnerListQueryUseCase;
import com.tastyhouse.application.shop.port.out.ShopNoticeOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopNoticeResult;

@Service
@Transactional(readOnly = true)
class ShopNoticeOwnerListQueryService implements ShopNoticeOwnerListQueryUseCase {

    private final ShopNoticeOwnerQueryPort shopNoticeOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopNoticeOwnerListQueryService(
        ShopNoticeOwnerQueryPort shopNoticeOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopNoticeOwnerQueryPort = shopNoticeOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public List<ShopNoticeResult> getNotices(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopNoticeOwnerQueryPort.findNotices(shopId);
    }
}
