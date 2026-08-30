package com.tastyhouse.infrastructure.sms.persistence;

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

import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;

/**
 * SMS 인증 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code SmsVerification}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code SmsVerificationMapper}가 수행한다.
 * (감사 필드가 {@code created_at}뿐이고 {@code updated_at}이 없어 {@code BaseEntity}를 상속하지 않는다.)
 *
 * <p>테이블·인덱스명은 채널 도메인 어휘에 맞춰 {@code SMS_VERIFICATION}·{@code idx_sms_verification_*}로
 * 통일되어 있다({@code alter.sql}의 RENAME 마이그레이션 참고 — {@code ddl-auto=validate} 환경이므로
 * 그 마이그레이션과 앱 배포는 원자적으로 수행해야 한다). 반면 {@code phone_number} 컬럼은 채널이 아니라
 * 검증 대상 데이터의 이름이므로 그대로 유지한다.
 */
@Entity
@Table(name = "SMS_VERIFICATION", indexes = {
    @Index(name = "idx_sms_verification_phone_number", columnList = "phone_number"),
    @Index(name = "idx_sms_verification_expires_at", columnList = "expires_at")
})
public class SmsVerificationJpaEntity {

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
    private SmsVerificationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SmsVerificationJpaEntity() {
    }

    private SmsVerificationJpaEntity(
        PhoneNumber phoneNumber,
        VerificationCode verificationCode,
        SmsVerificationStatus status,
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
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code SmsVerificationMapper#toEntity}에서만 호출한다.
     */
    static SmsVerificationJpaEntity create(
        PhoneNumber phoneNumber,
        VerificationCode verificationCode,
        SmsVerificationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        return new SmsVerificationJpaEntity(phoneNumber, verificationCode, status, expiresAt, verifiedAt, createdAt);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(SmsVerificationStatus status, LocalDateTime verifiedAt) {
        this.status = status;
        this.verifiedAt = verifiedAt;
    }

    public Long getId() {
        return this.id;
    }

    public PhoneNumber getPhoneNumber() {
        return this.phoneNumber;
    }

    public VerificationCode getVerificationCode() {
        return this.verificationCode;
    }

    public SmsVerificationStatus getStatus() {
        return this.status;
    }

    public LocalDateTime getExpiresAt() {
        return this.expiresAt;
    }

    public LocalDateTime getVerifiedAt() {
        return this.verifiedAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }
}
