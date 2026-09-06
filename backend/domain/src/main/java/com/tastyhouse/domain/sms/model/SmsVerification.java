package com.tastyhouse.domain.sms.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.sms.vo.SmsVerificationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.domain.shared.vo.VerificationCode;

public class SmsVerification {
    public static final int EXPIRATION_MINUTES = 5;

    private final Long id;
    private final PhoneNumber phoneNumber;
    private final VerificationCode verificationCode;
    private SmsVerificationStatus status;
    private final LocalDateTime expiresAt;
    private LocalDateTime verifiedAt;
    private final LocalDateTime createdAt;

    private SmsVerification(
        Long id,
        PhoneNumber phoneNumber,
        VerificationCode verificationCode,
        SmsVerificationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        this.id = id;
        this.phoneNumber = phoneNumber;
        this.verificationCode = verificationCode;
        this.status = status;
        this.expiresAt = expiresAt;
        this.verifiedAt = verifiedAt;
        this.createdAt = createdAt;
    }

    public static SmsVerification create(String phoneNumber) {
        LocalDateTime now = LocalDateTime.now();
        return new SmsVerification(
            null,
            new PhoneNumber(phoneNumber),
            VerificationCode.generate(),
            SmsVerificationStatus.PENDING,
            now.plusMinutes(EXPIRATION_MINUTES),
            null,
            now
        );
    }

    public static SmsVerification reconstitute(
        Long id,
        PhoneNumber phoneNumber,
        VerificationCode verificationCode,
        SmsVerificationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        return new SmsVerification(id, phoneNumber, verificationCode, status, expiresAt, verifiedAt, createdAt);
    }

    public SmsVerificationId getSmsVerificationId() {
        return SmsVerificationId.of(this.id);
    }

    public void verify(VerificationCode inputCode, LocalDateTime now) {
        if (now.isAfter(this.expiresAt)) {
            this.status = SmsVerificationStatus.EXPIRED;
            throw new BusinessException(ErrorCode.SMS_VERIFICATION_CODE_EXPIRED);
        }
        if (!this.verificationCode.equals(inputCode)) {
            throw new BusinessException(ErrorCode.SMS_VERIFICATION_CODE_MISMATCH);
        }
        this.status = SmsVerificationStatus.VERIFIED;
        this.verifiedAt = now;
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
