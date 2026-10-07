package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopNoticePersistencePort;

@Component
class ShopNoticeOwnerReader {

    private final ShopNoticePersistencePort shopNoticePersistencePort;

    public ShopNoticeOwnerReader(ShopNoticePersistencePort shopNoticePersistencePort) {
        this.shopNoticePersistencePort = shopNoticePersistencePort;
    }

    public ShopNotice loadOwnedNotice(Long shopId, Long noticeId) {
        ShopNotice notice = shopNoticePersistencePort.findById(noticeId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOTICE_NOT_FOUND));
        if (!notice.getShopId().equals(ShopId.of(shopId))) {
            throw new ResourceNotFoundException(ApplicationErrorCode.SHOP_NOTICE_NOT_FOUND);
        }
        return notice;
    }
}
