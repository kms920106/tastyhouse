package com.tastyhouse.core.domain.verification.domain.model;

import com.tastyhouse.core.domain.verification.domain.vo.EmailVerificationId;
import com.tastyhouse.core.domain.verification.domain.vo.VerificationCode;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
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

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "EMAIL_VERIFICATION", indexes = {
    @Index(name = "idx_email_verification_email", columnList = "email"),
    @Index(name = "idx_email_verification_expires_at", columnList = "expires_at")
})
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    private static final int EXPIRATION_MINUTES = 5;

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
    private EmailVerificationStatus status;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private EmailVerification(String email, VerificationCode verificationCode) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.status = EmailVerificationStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES);
        this.createdAt = LocalDateTime.now();
    }

    public static EmailVerification create(String email) {
        return new EmailVerification(email, VerificationCode.generate());
    }

    public EmailVerificationId getEmailVerificationId() {
        return new EmailVerificationId(this.id);
    }

    public void verify(VerificationCode inputCode, LocalDateTime now) {
        if (now.isAfter(this.expiresAt)) {
            this.status = EmailVerificationStatus.EXPIRED;
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }
        if (!this.verificationCode.equals(inputCode)) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }
        this.status = EmailVerificationStatus.VERIFIED;
        this.verifiedAt = now;
    }

    public void expire() {
        this.status = EmailVerificationStatus.EXPIRED;
    }
}
