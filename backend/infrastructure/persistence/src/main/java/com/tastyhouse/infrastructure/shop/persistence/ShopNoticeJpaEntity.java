package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "SHOP_NOTICE",
    indexes = {
        @Index(name = "idx_shop_notice_shop_id", columnList = "shop_id"),
        @Index(name = "idx_shop_notice_exposed", columnList = "shop_id, is_exposed, is_hidden")
    }
)
public class ShopNoticeJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_exposed", nullable = false)
    private boolean exposed;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    protected ShopNoticeJpaEntity() {
    }

    private ShopNoticeJpaEntity(Long shopId, String content, boolean exposed, boolean hidden) {
        this.shopId = shopId;
        this.content = content;
        this.exposed = exposed;
        this.hidden = hidden;
    }

    static ShopNoticeJpaEntity create(Long shopId, String content, boolean exposed, boolean hidden) {
        return new ShopNoticeJpaEntity(shopId, content, exposed, hidden);
    }

    void applyChanges(String content, boolean exposed, boolean hidden) {
        this.content = content;
        this.exposed = exposed;
        this.hidden = hidden;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
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
}
