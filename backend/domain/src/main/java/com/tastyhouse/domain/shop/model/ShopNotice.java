package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.shop.vo.ShopId;

public class ShopNotice {
    private final Long id;
    private final ShopId shopId;
    private String content;
    private boolean exposed;
    private boolean hidden;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ShopNotice(
        Long id,
        ShopId shopId,
        String content,
        boolean exposed,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.content = content;
        this.exposed = exposed;
        this.hidden = hidden;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static ShopNotice of(ShopId shopId, String content) {
        return new ShopNotice(null, shopId, content, false, false, null, null);
    }

    public static ShopNotice reconstitute(
        Long id,
        ShopId shopId,
        String content,
        boolean exposed,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ShopNotice(id, shopId, content, exposed, hidden, createdAt, updatedAt);
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void expose() {
        this.exposed = true;
    }

    public void unexpose() {
        this.exposed = false;
    }

    public void hide() {
        this.hidden = true;
    }

    public void unhide() {
        this.hidden = false;
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public String getContent() {
        return this.content;
    }

    public boolean isExposed() {
        return this.exposed;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
