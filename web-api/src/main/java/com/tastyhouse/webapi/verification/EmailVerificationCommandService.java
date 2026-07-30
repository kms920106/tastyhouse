package com.tastyhouse.webapi.verification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.verification.domain.service.EmailVerificationService;

/**
 * 이메일 인증 command 서비스(web-api).
 *
 * <p>발급·검증 규칙 자체는 도메인 서비스 {@link EmailVerificationService}가 소유하고, 이 서비스는
 * 트랜잭션 경계와 HTTP 경계 파라미터 전달만 담당한다(공통 지침 패턴 2).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationCommandService {

    private final EmailVerificationService emailVerificationService;

    @Transactional
    public void sendVerificationCode(String email) {
        emailVerificationService.issueForSignUp(email);
    }

    @Transactional
    public String confirmVerificationCode(String email, String verificationCode) {
        emailVerificationService.confirmForSignUp(email, verificationCode);
        log.info("이메일 인증 완료. email={}", email);
        return email;
    }
}
