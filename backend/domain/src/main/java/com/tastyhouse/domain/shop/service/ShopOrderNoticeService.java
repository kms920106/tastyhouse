package com.tastyhouse.domain.shop.service;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.repository.ShopOrderNoticeRepository;
import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopOrderNoticeService {
    private static final int MAX_CONTENT_LENGTH = 500;

    private final ShopOrderNoticeRepository shopOrderNoticeRepository;

    public ShopOrderNoticeService(ShopOrderNoticeRepository shopOrderNoticeRepository) {
        this.shopOrderNoticeRepository = shopOrderNoticeRepository;
    }

    public void upsert(ShopId shopId, String content) {
        String validated = validateContent(content);

        shopOrderNoticeRepository.findByShopId(shopId)
            .map(existing -> {
                existing.updateContent(validated);
                return shopOrderNoticeRepository.save(existing);
            })
            .orElseGet(() -> shopOrderNoticeRepository.save(ShopOrderNotice.of(shopId, validated)));
    }

    public void hide(ShopId shopId, String reason) {
        ShopOrderNotice notice = loadByShopId(shopId);
        notice.hide(reason);
        shopOrderNoticeRepository.save(notice);
    }

    public void unhide(ShopId shopId) {
        ShopOrderNotice notice = loadByShopId(shopId);
        notice.unhide();
        shopOrderNoticeRepository.save(notice);
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
        return shopOrderNoticeRepository.findByShopId(shopId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOP_ORDER_NOTICE_NOT_FOUND));
    }
}
