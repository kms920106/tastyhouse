package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopOrderNoticeId;

public class ShopOrderNotice {
    private final ShopOrderNoticeId id;
    private final ShopId shopId;
    private String content;
    private boolean hidden;
    private String hiddenReason;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopOrderNotice(
        ShopOrderNoticeId id,
        ShopId shopId,
        String content,
        boolean hidden,
        String hiddenReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.content = content;
        this.hidden = hidden;
        this.hiddenReason = hiddenReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopOrderNotice of(ShopId shopId, String content) {
        return new ShopOrderNotice(null, shopId, content, false, null, null, null);
    }

    public static ShopOrderNotice reconstitute(
        ShopOrderNoticeId id,
        ShopId shopId,
        String content,
        boolean hidden,
        String hiddenReason,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopOrderNotice(id, shopId, content, hidden, hiddenReason, createdAt, updatedAt);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void hide(String reason) {
        this.hidden = true;
        this.hiddenReason = reason;
    }

    public void unhide() {
        this.hidden = false;
        this.hiddenReason = null;
    }

    public ShopOrderNoticeId getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public String getHiddenReason() {
        return this.hiddenReason;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
