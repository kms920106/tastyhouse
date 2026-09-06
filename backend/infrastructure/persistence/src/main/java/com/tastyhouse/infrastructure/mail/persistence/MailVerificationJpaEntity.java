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

import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.domain.shared.vo.VerificationCode;

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
