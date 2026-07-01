package com.tastyhouse.core.domain.verification.domain.model;

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

import com.tastyhouse.core.domain.verification.domain.vo.PhoneVerificationId;
import com.tastyhouse.core.domain.verification.domain.vo.VerificationCode;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.vo.PhoneNumber;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "PHONE_VERIFICATION", indexes = {
    @Index(name = "idx_phone_verification_phone_number", columnList = "phone_number"),
    @Index(name = "idx_phone_verification_expires_at", columnList = "expires_at")
})
public class PhoneVerification {

    private static final int EXPIRATION_MINUTES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
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

    private PhoneVerification(String phoneNumber, VerificationCode verificationCode) {
        this.phoneNumber = new PhoneNumber(phoneNumber);
        this.verificationCode = verificationCode;
        this.status = PhoneVerificationStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        this.createdAt = LocalDateTime.now();
    }

    public static PhoneVerification create(String phoneNumber) {
        return new PhoneVerification(phoneNumber, VerificationCode.generate());
    }

    public PhoneVerificationId getPhoneVerificationId() {
        return new PhoneVerificationId(this.id);
    }

    public void verify(VerificationCode inputCode, LocalDateTime now) {
        if (now.isAfter(this.expiresAt)) {
            this.status = PhoneVerificationStatus.EXPIRED;
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_EXPIRED);
        }
        if (!this.verificationCode.equals(inputCode)) {
            throw new BusinessException(ErrorCode.VERIFICATION_CODE_MISMATCH);
        }
        this.status = PhoneVerificationStatus.VERIFIED;
        this.verifiedAt = now;
    }
}
