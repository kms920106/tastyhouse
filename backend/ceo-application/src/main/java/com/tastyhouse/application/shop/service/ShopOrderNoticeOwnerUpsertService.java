package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeOwnerUpsertUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeUpsertCommand;

@Service
@Transactional
class ShopOrderNoticeOwnerUpsertService implements ShopOrderNoticeOwnerUpsertUseCase {

    private final ShopOrderNoticeService shopOrderNoticeService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOrderNoticeOwnerUpsertService(
        ShopOrderNoticeService shopOrderNoticeService,
        ShopOwnershipValidator shopOwnershipValidator
    ) {
        this.shopOrderNoticeService = shopOrderNoticeService;
        this.shopOwnershipValidator = shopOwnershipValidator;
    }

    @Override
    public void upsertOrderNotice(ShopOrderNoticeUpsertCommand command) {
        Long ceoId = command.ceoId();
        Long shopId = command.shopId();
        String content = command.content();

        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        shopOrderNoticeService.upsert(ShopId.of(shopId), content);
    }
}
