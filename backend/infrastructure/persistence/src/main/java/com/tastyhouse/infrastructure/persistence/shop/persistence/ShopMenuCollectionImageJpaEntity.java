package com.tastyhouse.infrastructure.persistence.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_MENU_COLLECTION_IMAGE")
class ShopMenuCollectionImageJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ShopMenuCollectionImageJpaEntity() {
    }

    private ShopMenuCollectionImageJpaEntity(
        Long shopId,
        Long imageFileId,
        Integer sort,
        String status,
        String rejectReason
    ) {
        this.shopId = shopId;
        this.imageFileId = imageFileId;
        this.sort = sort;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    static ShopMenuCollectionImageJpaEntity create(
        Long shopId,
        Long imageFileId,
        Integer sort,
        String status,
        String rejectReason
    ) {
        return new ShopMenuCollectionImageJpaEntity(shopId, imageFileId, sort, status, rejectReason);
    }

    void applyChanges(Integer sort, String status, String rejectReason) {
        this.sort = sort;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getImageFileId() {
        return this.imageFileId;
    }

    public Integer getSort() {
        return this.sort;
    }

    public String getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
