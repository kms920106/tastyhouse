package com.tastyhouse.infrastructure.persistence.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_REQUEST_COMMENT")
public class ShopRequestCommentJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_request_index_id", nullable = false)
    private Long shopRequestIndexId;

    @Column(name = "author_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String authorType;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    protected ShopRequestCommentJpaEntity() {
    }

    private ShopRequestCommentJpaEntity(
        Long shopRequestIndexId,
        String authorType,
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
        String authorType,
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

    public String getAuthorType() {
        return this.authorType;
    }

    public Long getAuthorId() {
        return this.authorId;
    }

    public String getContent() {
        return this.content;
    }
}
