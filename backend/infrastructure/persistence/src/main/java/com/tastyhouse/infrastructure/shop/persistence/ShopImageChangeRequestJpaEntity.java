package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shared.model.ApprovalStatus;
import com.tastyhouse.domain.shop.model.ShopImageType;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_IMAGE_CHANGE_REQUEST")
public class ShopImageChangeRequestJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ShopImageType imageType;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ApprovalStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ShopImageChangeRequestJpaEntity() {
    }

    private ShopImageChangeRequestJpaEntity(
        Long shopId,
        ShopImageType imageType,
        Long imageFileId,
        ApprovalStatus status,
        String rejectReason
    ) {
        this.shopId = shopId;
        this.imageType = imageType;
        this.imageFileId = imageFileId;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    static ShopImageChangeRequestJpaEntity create(
        Long shopId,
        ShopImageType imageType,
        Long imageFileId,
        ApprovalStatus status,
        String rejectReason
    ) {
        return new ShopImageChangeRequestJpaEntity(shopId, imageType, imageFileId, status, rejectReason);
    }

    void applyChanges(ApprovalStatus status, String rejectReason) {
        this.status = status;
        this.rejectReason = rejectReason;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public ShopImageType getImageType() {
        return this.imageType;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public ApprovalStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
