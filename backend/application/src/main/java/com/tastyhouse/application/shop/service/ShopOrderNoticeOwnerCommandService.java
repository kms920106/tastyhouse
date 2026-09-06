package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.shop.service.ShopOrderNoticeService;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeOwnerCommandUseCase;
import com.tastyhouse.application.shop.port.in.ShopOrderNoticeUpsertCommand;

@Service
@CeoApp
@Transactional
public class ShopOrderNoticeOwnerCommandService implements ShopOrderNoticeOwnerCommandUseCase {

    private final ShopOrderNoticeService shopOrderNoticeService;
    private final ShopOwnershipValidator shopOwnershipValidator;

    public ShopOrderNoticeOwnerCommandService(
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
