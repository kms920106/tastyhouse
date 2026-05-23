package com.tastyhouse.core.entity.partnership;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "PARTNERSHIP_REQUEST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PartnershipRequest extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "business_name", nullable = false, length = 200)
    private String businessName; // 사업장(업체) 이름

    @Column(name = "address", nullable = false, length = 500)
    private String address; // 사업장 주소

    @Column(name = "address_detail", length = 500)
    private String addressDetail; // 사업장 상세 주소

    @Column(name = "contact_name", nullable = false, length = 100)
    private String contactName; // 담당자 이름

    @Column(name = "contact_phone", nullable = false, length = 20)
    private String contactPhone; // 담당자 연락처

    @Column(name = "consultation_requested_at", nullable = false)
    private LocalDateTime consultationRequestedAt; // 상담 신청 일시

    public PartnershipRequest(String businessName, String address, String addressDetail,
                            String contactName, String contactPhone, LocalDateTime consultationRequestedAt) {
        this.businessName = businessName;
        this.address = address;
        this.addressDetail = addressDetail;
        this.contactName = contactName;
        this.contactPhone = contactPhone;
        this.consultationRequestedAt = consultationRequestedAt;
    }

    public static PartnershipRequest of(String businessName, String address, String addressDetail,
                                       String contactName, String contactPhone, LocalDateTime consultationRequestedAt) {
        return new PartnershipRequest(businessName, address, addressDetail, contactName, contactPhone, consultationRequestedAt);
    }
}
