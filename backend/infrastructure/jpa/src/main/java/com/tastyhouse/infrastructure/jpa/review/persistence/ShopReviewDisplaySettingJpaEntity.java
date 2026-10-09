package com.tastyhouse.infrastructure.jpa.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.jpa.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_REVIEW_DISPLAY_SETTING")
class ShopReviewDisplaySettingJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "sort_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String sortType;

    protected ShopReviewDisplaySettingJpaEntity() {
    }

    private ShopReviewDisplaySettingJpaEntity(Long shopId, String sortType) {
        this.shopId = shopId;
        this.sortType = sortType;
    }

    static ShopReviewDisplaySettingJpaEntity create(Long shopId, String sortType) {
        return new ShopReviewDisplaySettingJpaEntity(shopId, sortType);
    }

    void applyChanges(String sortType) {
        this.sortType = sortType;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getSortType() {
        return this.sortType;
    }
}
