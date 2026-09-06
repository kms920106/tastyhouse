package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.domain.shop.model.DeliveryAreaAdjustmentStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(
    name = "SHOP_DELIVERY_AREA_ADJUSTMENT_REQUEST",
    indexes = {
        @Index(name = "idx_shop_delivery_area_adjustment_shop_id_status", columnList = "shop_id, status"),
        @Index(name = "idx_shop_delivery_area_adjustment_status", columnList = "status")
    }
)
public class ShopDeliveryAreaAdjustmentRequestJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "counterpart_shop_name", nullable = false)
    private String counterpartShopName;

    @Column(name = "counterpart_business_number", nullable = false, length = 12)
    private String counterpartBusinessNumber;

    @Column(name = "franchise_name", nullable = false)
    private String franchiseName;

    @Column(name = "reason", nullable = false, length = 1000)
    private String reason;

    @Column(name = "consent_file_id", nullable = false)
    private Long consentFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private DeliveryAreaAdjustmentStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    protected ShopDeliveryAreaAdjustmentRequestJpaEntity() {
    }

    private ShopDeliveryAreaAdjustmentRequestJpaEntity(
        Long shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        Long consentFileId,
        DeliveryAreaAdjustmentStatus status,
        String rejectReason
    ) {
        this.shopId = shopId;
        this.counterpartShopName = counterpartShopName;
        this.counterpartBusinessNumber = counterpartBusinessNumber;
        this.franchiseName = franchiseName;
        this.reason = reason;
        this.consentFileId = consentFileId;
        this.status = status;
        this.rejectReason = rejectReason;
    }

    static ShopDeliveryAreaAdjustmentRequestJpaEntity create(
        Long shopId,
        String counterpartShopName,
        String counterpartBusinessNumber,
        String franchiseName,
        String reason,
        Long consentFileId,
        DeliveryAreaAdjustmentStatus status,
        String rejectReason
    ) {
        return new ShopDeliveryAreaAdjustmentRequestJpaEntity(
            shopId,
            counterpartShopName,
            counterpartBusinessNumber,
            franchiseName,
            reason,
            consentFileId,
            status,
            rejectReason
        );
    }

    void applyChanges(DeliveryAreaAdjustmentStatus status, String rejectReason) {
        this.status = status;
        this.rejectReason = rejectReason;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public String getCounterpartShopName() {
        return this.counterpartShopName;
    }

    public String getCounterpartBusinessNumber() {
        return this.counterpartBusinessNumber;
    }

    public String getFranchiseName() {
        return this.franchiseName;
    }

    public String getReason() {
        return this.reason;
    }

    public Long getConsentFileId() {
        return this.consentFileId;
    }

    public DeliveryAreaAdjustmentStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }
}
