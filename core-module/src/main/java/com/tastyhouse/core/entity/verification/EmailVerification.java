package com.tastyhouse.core.entity.verification;

import jakarta.persistence.Column;
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

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "EMAIL_VERIFICATION", indexes = {
    @Index(name = "idx_email_verification_email", columnList = "email"),
    @Index(name = "idx_email_verification_expires_at", columnList = "expires_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    private static final int VERIFICATION_CODE_EXPIRATION_MINUTES = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "email", nullable = false, length = 100)
    private String email; // 인증 대상 이메일 주소

    @Column(name = "verification_code", nullable = false, length = 6)
    private String verificationCode; // 인증 코드 (6자리 숫자)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private EmailVerificationStatus status; // 인증 상태 (예: PENDING, VERIFIED, EXPIRED)

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 인증 코드 만료 일시

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt; // 인증 완료 일시

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 인증 요청 생성 일시

    private EmailVerification(
        String email,
        String verificationCode
    ) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.status = EmailVerificationStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES);
        this.createdAt = LocalDateTime.now();
    }

    public static EmailVerification of(
        String email,
        String verificationCode
    ) {
        return new EmailVerification(
            email,
            verificationCode
        );
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void verify() {
        this.status = EmailVerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = EmailVerificationStatus.EXPIRED;
    }
}
