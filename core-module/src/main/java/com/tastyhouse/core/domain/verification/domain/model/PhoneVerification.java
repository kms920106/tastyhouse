package com.tastyhouse.core.domain.verification.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

import com.tastyhouse.core.domain.verification.domain.vo.PhoneVerificationId;
import com.tastyhouse.core.domain.verification.domain.vo.VerificationCode;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.shared.vo.PhoneNumber;

/**
 * 휴대폰 인증 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code PhoneVerificationJpaEntity} + {@code PhoneVerificationMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 호출부가 명시적으로
 * {@code PhoneVerificationRepository#save}를 호출해야 한다.
 */
@Getter
public class PhoneVerification {

    private static final int EXPIRATION_MINUTES = 5;

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final PhoneNumber phoneNumber; // 인증 휴대폰 번호
    private final VerificationCode verificationCode; // 인증 코드 (6자리)
    private PhoneVerificationStatus status; // 인증 상태
    private final LocalDateTime expiresAt; // 만료 일시
    private LocalDateTime verifiedAt; // 인증 완료 일시
    private final LocalDateTime createdAt; // 생성 일시

    private PhoneVerification(
        Long id,
        PhoneNumber phoneNumber,
        VerificationCode verificationCode,
        PhoneVerificationStatus status,
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

    /**
     * 신규 휴대폰 인증을 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static PhoneVerification create(String phoneNumber) {
        LocalDateTime now = LocalDateTime.now();
        return new PhoneVerification(
            null,
            new PhoneNumber(phoneNumber),
            VerificationCode.generate(),
            PhoneVerificationStatus.PENDING,
            now.plusMinutes(EXPIRATION_MINUTES),
            null,
            now
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
    public static PhoneVerification reconstitute(
        Long id,
        PhoneNumber phoneNumber,
        VerificationCode verificationCode,
        PhoneVerificationStatus status,
        LocalDateTime expiresAt,
        LocalDateTime verifiedAt,
        LocalDateTime createdAt
    ) {
        return new PhoneVerification(id, phoneNumber, verificationCode, status, expiresAt, verifiedAt, createdAt);
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
