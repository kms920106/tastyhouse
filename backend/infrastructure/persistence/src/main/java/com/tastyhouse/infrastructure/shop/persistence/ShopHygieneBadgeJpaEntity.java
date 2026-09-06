package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.HygieneBadgeType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_HYGIENE_BADGE")
public class ShopHygieneBadgeJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "badge_type", nullable = false, length = 30, columnDefinition = "VARCHAR(30)")
    private HygieneBadgeType badgeType;

    @Column(name = "certified_date", nullable = false)
    private LocalDate certifiedDate;

    @Column(name = "last_inspection_month", length = 7)
    private String lastInspectionMonth;

    protected ShopHygieneBadgeJpaEntity() {
    }

    private ShopHygieneBadgeJpaEntity(Long shopId, HygieneBadgeType badgeType, LocalDate certifiedDate, String lastInspectionMonth) {
        this.shopId = shopId;
        this.badgeType = badgeType;
        this.certifiedDate = certifiedDate;
        this.lastInspectionMonth = lastInspectionMonth;
    }

    static ShopHygieneBadgeJpaEntity create(Long shopId, HygieneBadgeType badgeType, LocalDate certifiedDate, String lastInspectionMonth) {
        return new ShopHygieneBadgeJpaEntity(shopId, badgeType, certifiedDate, lastInspectionMonth);
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public HygieneBadgeType getBadgeType() {
        return this.badgeType;
    }

    public LocalDate getCertifiedDate() {
        return this.certifiedDate;
    }

    public String getLastInspectionMonth() {
        return this.lastInspectionMonth;
    }
}
