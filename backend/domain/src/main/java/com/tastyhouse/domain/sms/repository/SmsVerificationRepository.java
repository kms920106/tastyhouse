package com.tastyhouse.domain.sms.repository;

import java.util.Optional;

import com.tastyhouse.domain.sms.model.SmsVerification;
import com.tastyhouse.domain.sms.model.SmsVerificationStatus;

/**
 * SMS 인증 write 포트.
 *
 * <p>여기 남은 조회는 모두 상태 전이의 전제 조건이다 — {@code findLatestPendingByPhoneNumber}는
 * 코드 대조·만료 판정에, {@code expireAllPendingByPhoneNumber}는 "같은 번호의 기존 미완료 인증을
 * 모두 만료시킨다"는 발급 불변식에 필요하다. 표현 목적 조회는 이 포트에 두지 않는다.
 */
public interface SmsVerificationRepository {

    SmsVerification save(SmsVerification smsVerification);

    Optional<SmsVerification> findLatestPendingByPhoneNumber(String phoneNumber, SmsVerificationStatus status);

    void expireAllPendingByPhoneNumber(String phoneNumber);
}
