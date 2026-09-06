package com.tastyhouse.domain.mail.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.mail.vo.MailVerificationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.vo.VerificationCode;

public class MailVerification {
    public static final int EXPIRATION_MINUTES = 5;

    private final Long id;
    private final String email;
    private final VerificationCode verificationCode;
    private MailVerificationStatus status;
    private final LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    private final LocalDateTime createdAt;

    private MailVerification(
        Long id,
        String email,
        VerificationCode verificationCode,
        MailVerificationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.email = email;
        this.verificationCode = verificationCode;
        this.status = status;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
    }

    public static MailVerification create(String email) {
        LocalDateTime now = LocalDateTime.now();
        return new MailVerification(
            null,
            email,
            VerificationCode.generate(),
            MailVerificationStatus.PENDING,
            now.plusMinutes(EXPIRATION_MINUTES),
            null,
            now
        );
    }

    public static MailVerification reconstitute(
        Long id,
        String email,
        VerificationCode verificationCode,
        MailVerificationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        return new MailVerification(id, email, verificationCode, status, expiresAt, verifiedAt, createdAt);
    }

    public MailVerificationId getMailVerificationId() {
        return MailVerificationId.of(this.id);
    }

    public void verify(VerificationCode inputCode, LocalDateTime now) {
        if (now.isAfter(this.expiresAt)) {
            this.status = MailVerificationStatus.EXPIRED;
            throw new BusinessException(ErrorCode.MAIL_VERIFICATION_CODE_EXPIRED);
        }
        if (!this.verificationCode.equals(inputCode)) {
            throw new BusinessException(ErrorCode.MAIL_VERIFICATION_CODE_MISMATCH);
        }
        this.status = MailVerificationStatus.VERIFIED;
        this.verifiedAt = now;
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
