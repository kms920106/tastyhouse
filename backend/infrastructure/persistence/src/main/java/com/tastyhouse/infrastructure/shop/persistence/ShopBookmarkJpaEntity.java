package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_BOOKMARK")
public class ShopBookmarkJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "member_id", nullable = false)
    private Long memberId;

    protected ShopBookmarkJpaEntity() {
    }

    private ShopBookmarkJpaEntity(Long shopId, Long memberId) {
        this.shopId = shopId;
        this.memberId = memberId;
    }

    static ShopBookmarkJpaEntity create(Long shopId, Long memberId) {
        return new ShopBookmarkJpaEntity(shopId, memberId);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getMemberId() {
        return this.memberId;
    }
}
