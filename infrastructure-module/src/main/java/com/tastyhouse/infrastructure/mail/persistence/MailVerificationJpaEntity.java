package com.tastyhouse.infrastructure.mail.persistence;

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

import com.tastyhouse.domain.mail.domain.model.MailVerificationStatus;
import com.tastyhouse.domain.shared.vo.VerificationCode;

/**
 * 메일 인증 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code MailVerification}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code MailVerificationMapper}가 수행한다.
 * (감사 필드가 {@code created_at}뿐이고 {@code updated_at}이 없어 {@code BaseEntity}를 상속하지 않는다.)
 *
 * <p>테이블·인덱스명은 채널 도메인 어휘에 맞춰 {@code MAIL_VERIFICATION}·{@code idx_mail_verification_*}로
 * 통일되어 있다({@code alter.sql}의 RENAME 마이그레이션 참고 — {@code ddl-auto=validate} 환경이므로
 * 그 마이그레이션과 앱 배포는 원자적으로 수행해야 한다). 반면 {@code email} 컬럼·필드는 채널이 아니라
 * 검증 대상인 주소 값 자체라 이름을 유지한다.
 */
@Entity
@Table(name = "MAIL_VERIFICATION", indexes = {
    @Index(name = "idx_mail_verification_email", columnList = "email"),
    @Index(name = "idx_mail_verification_expires_at", columnList = "expires_at")
})
public class MailVerificationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "verification_code", nullable = false, length = 6))
    private VerificationCode verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private MailVerificationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected MailVerificationJpaEntity() {
    }

    private MailVerificationJpaEntity(
        String email,
        VerificationCode verificationCode,
        MailVerificationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.status = status;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code MailVerificationMapper#toEntity}에서만 호출한다.
     */
    static MailVerificationJpaEntity create(
        String email,
        VerificationCode verificationCode,
        MailVerificationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        return new MailVerificationJpaEntity(email, verificationCode, status, expiresAt, verifiedAt, createdAt);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(MailVerificationStatus status, LocalDateTime verifiedAt) {
        this.status = status;
        this.verifiedAt = verifiedAt;
    }

    public Long getId() {
        return this.id;
    }

    public String getEmail() {
        return this.email;
    }

    public VerificationCode getVerificationCode() {
        return this.verificationCode;
    }

    public MailVerificationStatus getStatus() {
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
