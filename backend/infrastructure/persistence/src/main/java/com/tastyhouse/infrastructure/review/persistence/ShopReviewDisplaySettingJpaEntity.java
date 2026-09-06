package com.tastyhouse.infrastructure.review.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.review.model.ReviewSortType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_REVIEW_DISPLAY_SETTING")
public class ShopReviewDisplaySettingJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "sort_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ReviewSortType sortType;

    protected ShopReviewDisplaySettingJpaEntity() {
    }

    private ShopReviewDisplaySettingJpaEntity(Long shopId, ReviewSortType sortType) {
        this.shopId = shopId;
        this.sortType = sortType;
    }

    static ShopReviewDisplaySettingJpaEntity create(Long shopId, ReviewSortType sortType) {
        return new ShopReviewDisplaySettingJpaEntity(shopId, sortType);
    }

    void applyChanges(ReviewSortType sortType) {
        this.sortType = sortType;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public ReviewSortType getSortType() {
        return this.sortType;
    }
}
