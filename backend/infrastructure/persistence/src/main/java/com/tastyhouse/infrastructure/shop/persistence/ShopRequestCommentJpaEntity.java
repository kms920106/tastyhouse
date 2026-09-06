package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_REQUEST_COMMENT")
public class ShopRequestCommentJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_request_index_id", nullable = false)
    private Long shopRequestIndexId;

    @Enumerated(EnumType.STRING)
    @Column(name = "author_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopRequestCommentAuthorType authorType;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    protected ShopRequestCommentJpaEntity() {
    }

    private ShopRequestCommentJpaEntity(
        Long shopRequestIndexId,
        ShopRequestCommentAuthorType authorType,
        Long authorId,
        String content
    ) {
        this.shopRequestIndexId = shopRequestIndexId;
        this.authorType = authorType;
        this.authorId = authorId;
        this.content = content;
    }

    static ShopRequestCommentJpaEntity create(
        Long shopRequestIndexId,
        ShopRequestCommentAuthorType authorType,
        Long authorId,
        String content
    ) {
        return new ShopRequestCommentJpaEntity(shopRequestIndexId, authorType, authorId, content);
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
}
