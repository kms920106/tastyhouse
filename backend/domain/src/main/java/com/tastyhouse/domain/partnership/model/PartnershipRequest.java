package com.tastyhouse.domain.partnership.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.partnership.vo.PartnershipRequestId;

public class PartnershipRequest {
    private final Long id;
    private final String businessName;
    private final String address;
    private final String addressDetail;
    private final String contactName;
    private final String contactPhone;
    private final LocalDateTime consultationRequestedAt;
    private PartnershipStatus status;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private PartnershipRequest(
        Long id,
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt,
        PartnershipStatus status,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.businessName = businessName;
        this.address = address;
        this.addressDetail = addressDetail;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.consultationRequestedAt = consultationRequestedAt;
        this.status = status;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static PartnershipRequest of(
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt
    ) {
        return new PartnershipRequest(
            null, businessName, address, addressDetail, contactName, contactPhone,
            consultationRequestedAt, PartnershipStatus.PENDING, false, null, null
        );
    }

    public static PartnershipRequest reconstitute(
        Long id,
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt,
        PartnershipStatus status,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new PartnershipRequest(
            id, businessName, address, addressDetail, contactName, contactPhone,
            consultationRequestedAt, status, deleted, createdAt, updatedAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public String getBusinessName() {
        return this.businessName;
    }

    public String getAddress() {
        return this.address;
    }

    public String getAddressDetail() {
        return this.addressDetail;
    }

    public String getContactName() {
        return this.contactName;
    }

    public String getContactPhone() {
        return this.contactPhone;
    }

    public LocalDateTime getConsultationRequestedAt() {
        return this.consultationRequestedAt;
    }

    public PartnershipStatus getStatus() {
        return this.status;
    }

    public boolean isDeleted() {
        return this.deleted;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public PartnershipRequestId getPartnershipRequestId() {
        return PartnershipRequestId.of(this.id);
    }

    public void changeStatus(PartnershipStatus status) {
        this.status = status;
    }

    public void delete() {
        this.deleted = true;
    }
}
