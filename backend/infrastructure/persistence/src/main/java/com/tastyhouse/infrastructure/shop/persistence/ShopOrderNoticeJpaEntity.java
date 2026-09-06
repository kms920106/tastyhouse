package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "SHOP_ORDER_NOTICE",
    uniqueConstraints = @UniqueConstraint(name = "uk_shop_order_notice_shop_id", columnNames = "shop_id")
)
public class ShopOrderNoticeJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "is_hidden", nullable = false)
    private boolean hidden;

    @Column(name = "hidden_reason", length = 500)
    private String hiddenReason;

    protected ShopOrderNoticeJpaEntity() {
    }

    private ShopOrderNoticeJpaEntity(Long shopId, String content, boolean hidden, String hiddenReason) {
        this.shopId = shopId;
        this.content = content;
        this.hidden = hidden;
        this.hiddenReason = hiddenReason;
    }

    static ShopOrderNoticeJpaEntity create(Long shopId, String content, boolean hidden, String hiddenReason) {
        return new ShopOrderNoticeJpaEntity(shopId, content, hidden, hiddenReason);
    }

    void applyChanges(String content, boolean hidden, String hiddenReason) {
        this.content = content;
        this.hidden = hidden;
        this.hiddenReason = hiddenReason;
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

    public boolean isHidden() {
        return this.hidden;
    }

    public String getHiddenReason() {
        return this.hiddenReason;
    }
}
