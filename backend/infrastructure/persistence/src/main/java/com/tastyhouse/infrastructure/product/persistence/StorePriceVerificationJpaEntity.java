package com.tastyhouse.infrastructure.product.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.product.model.StorePriceVerificationStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_STORE_PRICE_VERIFICATION")
public class StorePriceVerificationJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "price_list_file_id", nullable = false)
    private Long priceListFileId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private StorePriceVerificationStatus status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "requested_by_ceo_id")
    private Long requestedByCeoId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    protected StorePriceVerificationJpaEntity() {
    }

    private StorePriceVerificationJpaEntity(
        Long shopId,
        Long priceListFileId,
        StorePriceVerificationStatus status,
        String rejectReason,
        Long requestedByCeoId,
        LocalDateTime processedAt
    ) {
        this.shopId = shopId;
        this.priceListFileId = priceListFileId;
        this.status = status;
        this.rejectReason = rejectReason;
        this.requestedByCeoId = requestedByCeoId;
        this.processedAt = processedAt;
    }

    static StorePriceVerificationJpaEntity create(
        Long shopId,
        Long priceListFileId,
        StorePriceVerificationStatus status,
        String rejectReason,
        Long requestedByCeoId,
        LocalDateTime processedAt
    ) {
        return new StorePriceVerificationJpaEntity(
            shopId,
            priceListFileId,
            status,
            rejectReason,
            requestedByCeoId,
            processedAt
        );
    }

    void applyChanges(
        StorePriceVerificationStatus status,
        String rejectReason,
        LocalDateTime processedAt
    ) {
        this.status = status;
        this.rejectReason = rejectReason;
        this.processedAt = processedAt;
    }

    public Long getId() {
        return this.id;
    }

    public Long getShopId() {
        return this.shopId;
    }

    public Long getPriceListFileId() {
        return this.priceListFileId;
    }

    public StorePriceVerificationStatus getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public Long getRequestedByCeoId() {
        return this.requestedByCeoId;
    }

    public LocalDateTime getProcessedAt() {
        return this.processedAt;
    }
}
