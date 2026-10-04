package com.tastyhouse.infrastructure.persistence.sms.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.persistence.shared.persistence.PhoneNumberEmbeddable;
import com.tastyhouse.infrastructure.persistence.shared.persistence.VerificationCodeEmbeddable;

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
    private PhoneNumberEmbeddable phoneNumber;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "verification_code", nullable = false, length = 6))
    private VerificationCodeEmbeddable verificationCode;

    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private String status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    protected SmsVerificationJpaEntity() {
    }

    private SmsVerificationJpaEntity(
        PhoneNumberEmbeddable phoneNumber,
        VerificationCodeEmbeddable verificationCode,
        String status,
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

    static SmsVerificationJpaEntity create(
        PhoneNumberEmbeddable phoneNumber,
        VerificationCodeEmbeddable verificationCode,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        return new SmsVerificationJpaEntity(phoneNumber, verificationCode, status, expiresAt, verifiedAt, createdAt);
    }

    void applyChanges(String status, LocalDateTime verifiedAt) {
        this.status = status;
        this.verifiedAt = verifiedAt;
    }

    public Long getId() {
        return this.id;
    }

    public PhoneNumberEmbeddable getPhoneNumber() {
        return this.phoneNumber;
    }

    public VerificationCodeEmbeddable getVerificationCode() {
        return this.verificationCode;
    }

    public String getStatus() {
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
