package com.tastyhouse.core.entity.verification;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "EMAIL_VERIFICATION", indexes = {
    @Index(name = "idx_email_verification_email", columnList = "email"),
    @Index(name = "idx_email_verification_expires_at", columnList = "expires_at")
})
public class EmailVerification {

    private static final int VERIFICATION_CODE_EXPIRATION_MINUTES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "verification_code", nullable = false, length = 6)
    private String verificationCode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private EmailVerificationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public EmailVerification(String email, String verificationCode) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.status = EmailVerificationStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES);
        this.createdAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isVerified() {
        return this.status == EmailVerificationStatus.VERIFIED;
    }

    public void verify() {
        this.status = EmailVerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = EmailVerificationStatus.EXPIRED;
    }
}
