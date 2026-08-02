package com.tastyhouse.domain.sms.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.sms.domain.vo.SmsVerificationId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.vo.PhoneNumber;
import com.tastyhouse.domain.shared.vo.VerificationCode;

/**
 * SMS 인증 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code SmsVerificationJpaEntity} + {@code SmsVerificationMapper}가 담당한다. 도메인이
 * 프레임워크-프리이므로 변경 후 저장은 더티 체킹이 아니라 호출부가 명시적으로
 * {@code SmsVerificationRepository#save}를 호출해야 한다.
 *
 * <p>타입명은 채널(SMS)을 따르지만 {@code phoneNumber} 필드는 검증 대상인 번호 값 자체이므로
 * 그 이름을 유지한다(공용 VO {@link PhoneNumber}).
 */
public class SmsVerification {

    /**
     * 인증코드 유효시간(분). 발송 문구에도 이 값이 노출되어야 하므로
     * {@code SmsVerificationMessage}가 참조할 수 있게 공개한다 — 양쪽에 리터럴을 복제하면
     * 한쪽만 바뀌어 "실제 만료는 10분인데 안내는 5분" 같은 불일치가 조용히 생긴다.
     */
    public static final int EXPIRATION_MINUTES = 5;

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final PhoneNumber phoneNumber; // 인증 휴대폰 번호
    private final VerificationCode verificationCode; // 인증 코드 (6자리)
    private SmsVerificationStatus status; // 인증 상태
    private final LocalDateTime expiresAt; // 만료 일시
    private LocalDateTime verifiedAt; // 인증 완료 일시
    private final LocalDateTime createdAt; // 생성 일시

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

    /**
     * 신규 SMS 인증을 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
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

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     */
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
