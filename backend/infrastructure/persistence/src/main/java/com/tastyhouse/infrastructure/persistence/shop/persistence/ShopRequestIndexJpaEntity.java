package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.BaseEntity;

@Entity
@Table(name = "SHOP_REQUEST_INDEX")
public class ShopRequestIndexJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @Column(name = "request_type", nullable = false, length = 40, columnDefinition = "VARCHAR(40)")
    private String requestType;

    @Column(name = "source_request_id", nullable = false)
    private Long sourceRequestId;

    @Column(name = "summary", nullable = false)
    private String summary;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "attachment_file_id")
    private Long attachmentFileId;

    @Column(name = "requested_by_ceo_id")
    private Long requestedByCeoId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    protected ShopRequestIndexJpaEntity() {
    }

    private ShopRequestIndexJpaEntity(
        Long shopId,
        String requestType,
        Long sourceRequestId,
        String summary,
        String status,
        String rejectReason,
        Long attachmentFileId,
        Long requestedByCeoId,
        LocalDateTime processedAt
    ) {
        this.shopId = shopId;
        this.requestType = requestType;
        this.sourceRequestId = sourceRequestId;
        this.summary = summary;
        this.status = status;
        this.rejectReason = rejectReason;
        this.attachmentFileId = attachmentFileId;
        this.requestedByCeoId = requestedByCeoId;
        this.processedAt = processedAt;
    }

    static ShopRequestIndexJpaEntity create(
        Long shopId,
        String requestType,
        Long sourceRequestId,
        String summary,
        String status,
        String rejectReason,
        Long attachmentFileId,
        Long requestedByCeoId,
        LocalDateTime processedAt
    ) {
        return new ShopRequestIndexJpaEntity(shopId, requestType, sourceRequestId, summary, status, rejectReason,
            attachmentFileId, requestedByCeoId, processedAt);
    }

    void applyChanges(String status, String rejectReason, LocalDateTime processedAt) {
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

    public String getRequestType() {
        return this.requestType;
    }

    public Long getSourceRequestId() {
        return this.sourceRequestId;
    }

    public String getSummary() {
        return this.summary;
    }

    public String getStatus() {
        return this.status;
    }

    public String getRejectReason() {
        return this.rejectReason;
    }

    public Long getAttachmentFileId() {
        return this.attachmentFileId;
    }

    public Long getRequestedByCeoId() {
        return this.requestedByCeoId;
    }

    public LocalDateTime getProcessedAt() {
        return this.processedAt;
    }
}
