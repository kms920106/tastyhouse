package com.tastyhouse.core.entity.verification;

import com.tastyhouse.core.entity.common.vo.PhoneNumber;
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
    private Long id; // PK

    @Embedded
    private PhoneNumber phoneNumber; // 인증 대상 휴대폰 번호 (임베디드 VO)

    @Column(name = "verification_code", nullable = false, length = 6)
    private String verificationCode; // 인증 코드 (6자리 숫자)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private PhoneVerificationStatus status; // 인증 상태 (예: PENDING, VERIFIED, EXPIRED)

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // 인증 코드 만료 일시

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt; // 인증 완료 일시

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt; // 인증 요청 생성 일시

    private PhoneVerification(
        String phoneNumber,
        String verificationCode
    ) {
        this.phoneNumber = new PhoneNumber(phoneNumber);
        this.verificationCode = verificationCode;
        this.status = PhoneVerificationStatus.PENDING;
        this.expiresAt = LocalDateTime.now().plusMinutes(VERIFICATION_CODE_EXPIRATION_MINUTES);
        this.createdAt = LocalDateTime.now();
    }

    public static PhoneVerification of(
        String phoneNumber,
        String verificationCode
    ) {
        return new PhoneVerification(
            phoneNumber,
            verificationCode
        );
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public void verify() {
        this.status = PhoneVerificationStatus.VERIFIED;
        this.verifiedAt = LocalDateTime.now();
    }

    public void expire() {
        this.status = PhoneVerificationStatus.EXPIRED;
    }
}
