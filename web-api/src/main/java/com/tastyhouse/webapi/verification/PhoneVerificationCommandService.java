package com.tastyhouse.webapi.verification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.verification.domain.service.PhoneVerificationService;

/**
 * 휴대폰 인증 command 서비스(web-api).
 *
 * <p>발급·검증 규칙 자체는 도메인 서비스 {@link PhoneVerificationService}가 소유하고, 이 서비스는
 * 트랜잭션 경계와 HTTP 경계 파라미터 전달만 담당한다(공통 지침 패턴 2).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationCommandService {

    private final PhoneVerificationService phoneVerificationService;

    @Transactional
    public void sendVerificationCode(String phoneNumber) {
        phoneVerificationService.issue(phoneNumber);
    }

    @Transactional
    public String confirmVerificationCode(String phoneNumber, String verificationCode) {
        phoneVerificationService.confirm(phoneNumber, verificationCode);
        log.info("휴대폰 인증 완료. phoneNumber={}", phoneNumber);
        return phoneNumber;
    }
}
