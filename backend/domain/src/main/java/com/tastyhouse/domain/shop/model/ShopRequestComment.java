package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

public class ShopRequestComment {
    private final Long id;
    private final Long shopRequestIndexId;
    private final ShopRequestCommentAuthorType authorType;
    private final Long authorId;
    private final String content;
    private final LocalDateTime createdAt;

    private ShopRequestComment(
        Long id,
        Long shopRequestIndexId,
        ShopRequestCommentAuthorType authorType,
        Long authorId,
        String content,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.shopRequestIndexId = shopRequestIndexId;
        this.authorType = authorType;
        this.authorId = authorId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public static ShopRequestComment of(Long shopRequestIndexId, ShopRequestCommentAuthor author, String content) {
        return new ShopRequestComment(null, shopRequestIndexId, author.authorType(), author.authorId(), content, null);
    }

    public static ShopRequestComment reconstitute(
        Long id,
        Long shopRequestIndexId,
        ShopRequestCommentAuthorType authorType,
        Long authorId,
        String content,
        LocalDateTime createdAt
    ) {
        return new ShopRequestComment(id, shopRequestIndexId, authorType, authorId, content, createdAt);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopRequestIndexId() {
        return this.shopRequestIndexId;
    }

    public ShopRequestCommentAuthorType getAuthorType() {
        return this.authorType;
    }

    public Long getAuthorId() {
        return this.authorId;
    }

    public String getContent() {
        return this.content;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
