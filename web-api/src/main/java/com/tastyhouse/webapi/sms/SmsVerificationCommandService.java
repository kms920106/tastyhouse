package com.tastyhouse.webapi.sms;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.sms.domain.service.SmsVerificationService;

/**
 * SMS 인증 command 서비스(web-api).
 *
 * <p>발급(발송 포함)·검증 규칙 자체는 도메인 서비스 {@link SmsVerificationService}가 소유하고,
 * 이 서비스는 트랜잭션 경계와 HTTP 경계 파라미터 전달만 담당한다(공통 지침 패턴 2).
 *
 * <p>발송은 도메인 서비스의 {@code issue} 안에서 수행되므로 이 서비스가 별도로 발송을 호출하지
 * 않는다 — 과거 이 계층에서 발송을 호출하던 구조가 발송 누락 버그의 원인이었다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsVerificationCommandService {

    private final SmsVerificationService smsVerificationService;

    @Transactional
    public void sendVerificationCode(String phoneNumber) {
        smsVerificationService.issue(phoneNumber);
    }

    @Transactional
    public String confirmVerificationCode(String phoneNumber, String verificationCode) {
        smsVerificationService.confirm(phoneNumber, verificationCode);
        log.info("SMS 인증 완료. phoneNumber={}", phoneNumber);
        return phoneNumber;
    }
}
