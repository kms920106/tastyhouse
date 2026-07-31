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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.domain.partnership.domain.model.PartnershipStatus;
import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 제휴 문의 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code PartnershipRequest}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code PartnershipRequestMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "PARTNERSHIP_REQUEST")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code PartnershipRequestMapper#toEntity}에서만 호출한다.
     */
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

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자·불변 필드는 건드리지 않는다.
     */
    void applyChanges(PartnershipStatus status, boolean deleted) {
        this.status = status;
        this.deleted = deleted;
    }
}
