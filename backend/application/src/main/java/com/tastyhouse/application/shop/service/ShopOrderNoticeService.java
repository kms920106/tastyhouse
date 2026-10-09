package com.tastyhouse.application.shop.service;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeLoadPort;
import com.tastyhouse.application.shop.port.out.write.ShopOrderNoticeSavePort;

@Service
public class ShopOrderNoticeService {

    private static final int MAX_CONTENT_LENGTH = 500;

    private final ShopOrderNoticeLoadPort shopOrderNoticeLoadPort;
    private final ShopOrderNoticeSavePort shopOrderNoticeSavePort;

    public ShopOrderNoticeService(ShopOrderNoticeLoadPort shopOrderNoticeLoadPort, ShopOrderNoticeSavePort shopOrderNoticeSavePort) {
        this.shopOrderNoticeLoadPort = shopOrderNoticeLoadPort;
        this.shopOrderNoticeSavePort = shopOrderNoticeSavePort;
    }

    public void upsert(ShopId shopId, String content) {
        String validated = validateContent(content);

        shopOrderNoticeLoadPort.findByShopId(shopId)
            .map(existing -> {
                existing.updateContent(validated);
                return shopOrderNoticeSavePort.save(existing);
            })
            .orElseGet(() -> shopOrderNoticeSavePort.save(ShopOrderNotice.of(shopId, validated)));
    }

    public void hide(ShopId shopId, String reason) {
        ShopOrderNotice notice = loadByShopId(shopId);
        notice.hide(reason);
        shopOrderNoticeSavePort.save(notice);
    }

    public void unhide(ShopId shopId) {
        ShopOrderNotice notice = loadByShopId(shopId);
        notice.unhide();
        shopOrderNoticeSavePort.save(notice);
    }

    private String validateContent(String content) {
        if (content == null || content.isBlank()) {
            throw new ApplicationException(ApplicationErrorCode.SHOP_ORDER_NOTICE_CONTENT_REQUIRED);
        }

        String trimmed = content.trim();
        if (trimmed.length() > MAX_CONTENT_LENGTH) {
            throw new ApplicationException(ApplicationErrorCode.SHOP_ORDER_NOTICE_CONTENT_TOO_LONG);
        }
        return trimmed;
    }

    private ShopOrderNotice loadByShopId(ShopId shopId) {
        return shopOrderNoticeLoadPort.findByShopId(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.SHOP_ORDER_NOTICE_NOT_FOUND));
    }
}
