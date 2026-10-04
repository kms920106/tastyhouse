package com.tastyhouse.infrastructure.persistence.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_IMAGE_CHANGE_REQUEST")
class ShopImageChangeRequestJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "image_type", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String imageType;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ShopImageChangeRequestJpaEntity() {
    }

    private ShopImageChangeRequestJpaEntity(
        Long shopId,
        String imageType,
        Long imageFileId,
        String status,
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
        String imageType,
        Long imageFileId,
        String status,
        String rejectReason
    ) {
        return new ShopImageChangeRequestJpaEntity(shopId, imageType, imageFileId, status, rejectReason);
    }

    void applyChanges(String status, String rejectReason) {
        this.status = status;
        this.rejectReason = rejectReason;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getImageType() {
        return this.imageType;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public String getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
