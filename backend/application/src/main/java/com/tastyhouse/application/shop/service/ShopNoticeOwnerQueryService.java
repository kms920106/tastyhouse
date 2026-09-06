package com.tastyhouse.application.shop.service;

import com.tastyhouse.application.shared.marker.CeoApp;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.shop.port.in.ShopNoticeOwnerQueryUseCase;
import com.tastyhouse.domain.shop.service.ProhibitedWordValidator;
import com.tastyhouse.application.shop.port.out.ShopNoticeOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopNoticeResult;

@Service
@CeoApp
@Transactional(readOnly = true)
public class ShopNoticeOwnerQueryService implements ShopNoticeOwnerQueryUseCase {

    private final ShopNoticeOwnerQueryPort shopNoticeOwnerQueryPort;
    private final ShopOwnershipValidator shopOwnershipValidator;
    private final ProhibitedWordValidator prohibitedWordValidator;

    public ShopNoticeOwnerQueryService(
        ShopNoticeOwnerQueryPort shopNoticeOwnerQueryPort,
        ShopOwnershipValidator shopOwnershipValidator,
        ProhibitedWordValidator prohibitedWordValidator
    ) {
        this.shopNoticeOwnerQueryPort = shopNoticeOwnerQueryPort;
        this.shopOwnershipValidator = shopOwnershipValidator;
        this.prohibitedWordValidator = prohibitedWordValidator;
    }

    @Override
    public List<ShopNoticeResult> getNotices(Long ceoId, Long shopId) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);

        return shopNoticeOwnerQueryPort.findNotices(shopId);
    }

    @Override
    public List<String> validateNotice(Long ceoId, Long shopId, String content) {
        shopOwnershipValidator.validateOwnership(ceoId, shopId);
        return prohibitedWordValidator.findViolations(content);
    }

}
