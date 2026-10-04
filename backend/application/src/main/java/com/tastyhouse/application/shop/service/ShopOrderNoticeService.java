package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticePersistencePort;

@Service
public class ShopOrderNoticeService {

    private static final int MAX_CONTENT_LENGTH = 500;

    private final ShopOrderNoticePersistencePort shopOrderNoticePersistencePort;

    public ShopOrderNoticeService(ShopOrderNoticePersistencePort shopOrderNoticePersistencePort) {
        this.shopOrderNoticePersistencePort = shopOrderNoticePersistencePort;
    }

    public void upsert(ShopId shopId, String content) {
        String validated = validateContent(content);

        shopOrderNoticePersistencePort.findByShopId(shopId)
            .map(existing -> {
                existing.updateContent(validated);
                return shopOrderNoticePersistencePort.save(existing);
            })
            .orElseGet(() -> shopOrderNoticePersistencePort.save(ShopOrderNotice.of(shopId, validated)));
    }

    public void hide(ShopId shopId, String reason) {
        ShopOrderNotice notice = loadByShopId(shopId);
        notice.hide(reason);
        shopOrderNoticePersistencePort.save(notice);
    }

    public void unhide(ShopId shopId) {
        ShopOrderNotice notice = loadByShopId(shopId);
        notice.unhide();
        shopOrderNoticePersistencePort.save(notice);
    }

    private String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new BusinessException(ErrorCode.SHOP_ORDER_NOTICE_CONTENT_REQUIRED);
        }

        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new BusinessException(ErrorCode.SHOP_ORDER_NOTICE_CONTENT_TOO_LONG);
        }
        return trimmed;
    }

    private ShopOrderNotice loadByShopId(ShopId shopId) {
        return shopOrderNoticePersistencePort.findByShopId(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_ORDER_NOTICE_NOT_FOUND));
    }
}
