package com.tastyhouse.core.entity.verification;

import com.tastyhouse.core.entity.common.vo.PhoneNumber;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "PHONE_VERIFICATION", indexes = {
    @Index(name = "idx_phone_verification_phone_number", columnList = "phone_number"),
    @Index(name = "idx_phone_verification_expires_at", columnList = "expires_at")
})
public class PhoneVerification {

    private static final int VERIFICATION_CODE_EXPIRATION_MINUTES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private PhoneNumber phoneNumber;

    @Column(name = "verification_code", nullable = false, length = 6)
    private String verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private PhoneVerificationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public PhoneVerification(String phoneNumber, String verificationCode) {
        this.phoneNumber = new PhoneNumber(phoneNumber);
        this.verificationCode = verificationCode;
        this.status = PhoneVerificationStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES);
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isVerified() {
        return this.status == PhoneVerificationStatus.VERIFIED;
    }

    public void verify() {
        this.status = PhoneVerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = PhoneVerificationStatus.EXPIRED;
    }
}
