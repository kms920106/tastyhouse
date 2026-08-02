package com.tastyhouse.webapi.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.mail.domain.service.MailVerificationService;

/**
 * 메일 인증 command 서비스(web-api).
 *
 * <p>발급(발송 포함)·검증 규칙 자체는 도메인 서비스 {@link MailVerificationService}가 소유하고,
 * 이 서비스는 트랜잭션 경계와 HTTP 경계 파라미터 전달만 담당한다(공통 지침 패턴 2).
 *
 * <p>발송은 도메인 서비스의 {@code issueForSignUp} 안에서 수행되므로 이 서비스가 별도로 발송을
 * 호출하지 않는다 — 과거 이 계층에서 발송 호출이 누락되어 인증코드가 저장만 되던 버그가 있었다.
 */
@Service
public class MailVerificationCommandService {

    private static final Logger log = LoggerFactory.getLogger(MailVerificationCommandService.class);

    private final MailVerificationService mailVerificationService;

    public MailVerificationCommandService(MailVerificationService mailVerificationService) {
        this.mailVerificationService = mailVerificationService;
    }

    @Transactional
    public void sendVerificationCode(String email) {
        mailVerificationService.issueForSignUp(email);
    }

    @Transactional
    public String confirmVerificationCode(String email, String verificationCode) {
        mailVerificationService.confirmForSignUp(email, verificationCode);
        log.info("메일 인증 완료. email={}", email);
        return email;
    }
}
