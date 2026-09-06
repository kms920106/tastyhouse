package com.tastyhouse.infrastructure.partnership.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.domain.partnership.model.PartnershipStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "PARTNERSHIP_REQUEST")
public class PartnershipRequestJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName;

    @Column(name = "address", nullable = false, length = 500)
    private String address;

    @Column(name = "address_detail", length = 500)
    private String addressDetail;

    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName;

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone;

    @Column(name = "consultation_requested_at", nullable = false)
    private LocalDateTime consultationRequestedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private PartnershipStatus status;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    protected PartnershipRequestJpaEntity() {
    }

    private PartnershipRequestJpaEntity(
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt,
        PartnershipStatus status,
        boolean deleted
    ) {
        this.businessName = businessName;
        this.address = address;
        this.addressDetail = addressDetail;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.consultationRequestedAt = consultationRequestedAt;
        this.status = status;
        this.deleted = deleted;
    }

    static PartnershipRequestJpaEntity create(
        String businessName,
        String address,
        String addressDetail,
        String contactName,
        String contactPhone,
        LocalDateTime consultationRequestedAt,
        PartnershipStatus status,
        boolean deleted
    ) {
        return new PartnershipRequestJpaEntity(
            businessName, address, addressDetail, contactName, contactPhone,
            consultationRequestedAt, status, deleted
        );
    }

    void applyChanges(PartnershipStatus status, boolean deleted) {
        this.status = status;
        this.deleted = deleted;
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
}
