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
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_MENU_COLLECTION_IMAGE")
public class ShopMenuCollectionImageJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "image_file_id", nullable = false)
    private Long imageFileId;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private ApprovalStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ShopMenuCollectionImageJpaEntity() {
    }

    private ShopMenuCollectionImageJpaEntity(
        Long shopId,
        Long imageFileId,
        Integer sort,
        ApprovalStatus status,
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
        ApprovalStatus status,
        String rejectReason
    ) {
        return new ShopMenuCollectionImageJpaEntity(shopId, imageFileId, sort, status, rejectReason);
    }

    void applyChanges(Integer sort, ApprovalStatus status, String rejectReason) {
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

    public ApprovalStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
