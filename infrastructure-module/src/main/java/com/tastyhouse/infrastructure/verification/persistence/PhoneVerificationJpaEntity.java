package com.tastyhouse.infrastructure.verification.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.domain.verification.domain.model.PhoneVerificationStatus;
import com.tastyhouse.domain.verification.domain.vo.VerificationCode;
import com.tastyhouse.domain.shared.vo.PhoneNumber;

/**
 * 휴대폰 인증 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code PhoneVerification}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code PhoneVerificationMapper}가 수행한다.
 * (감사 필드가 {@code created_at}뿐이고 {@code updated_at}이 없어 {@code BaseEntity}를 상속하지 않는다.)
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "PHONE_VERIFICATION", indexes = {
    @Index(name = "idx_phone_verification_phone_number", columnList = "phone_number"),
    @Index(name = "idx_phone_verification_expires_at", columnList = "expires_at")
})
public class PhoneVerificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "phone_number", nullable = false, length = 11))
    private PhoneNumber phoneNumber;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "verification_code", nullable = false, length = 6))
    private VerificationCode verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private PhoneVerificationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private PhoneVerificationJpaEntity(
        PhoneNumber phoneNumber,
        VerificationCode verificationCode,
        PhoneVerificationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
        this.status = status;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code PhoneVerificationMapper#toEntity}에서만 호출한다.
     */
    static PhoneVerificationJpaEntity create(
        PhoneNumber phoneNumber,
        VerificationCode verificationCode,
        PhoneVerificationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        return new PhoneVerificationJpaEntity(phoneNumber, verificationCode, status, expiresAt, verifiedAt, createdAt);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(PhoneVerificationStatus status, LocalDateTime verifiedAt) {
        this.status = status;
        this.verifiedAt = verifiedAt;
    }
}
