package com.tastyhouse.webapi.verification;

import com.tastyhouse.core.entity.verification.EmailVerification;
import com.tastyhouse.core.entity.verification.EmailVerificationStatus;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.EmailVerificationCoreService;
import com.tastyhouse.core.service.MemberCoreService;
import com.tastyhouse.external.email.EmailSender;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String EMAIL_SUBJECT = "[TastyHouse] 이메일 인증번호 안내";
    private static final String EMAIL_BODY_TEMPLATE = "[TastyHouse] 인증번호 [%s]를 입력해주세요. (5분 내 유효)";

    private final MemberCoreService memberCoreService;
    private final EmailVerificationCoreService emailVerificationCoreService;
    private final EmailSender emailSender;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void sendVerificationCode(String email) {
        if (memberCoreService.existsByUsername(email)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_REGISTERED);
        }

        emailVerificationCoreService.expireAllPendingByEmail(email);

        String verificationCode = generateVerificationCode();

        emailVerificationCoreService.save(
            EmailVerification.builder()
                .email(email)
                .verificationCode(verificationCode)
                .build()
        );

        String emailBody = EMAIL_BODY_TEMPLATE.formatted(verificationCode);
        emailSender.send(email, EMAIL_SUBJECT, emailBody);

        log.info("이메일 인증번호 발송 완료. email: {}", email);
    }

    @Transactional
    public String confirmVerificationCode(String email, String verificationCode) {
        EmailVerification verification = emailVerificationCoreService
            .findLatestPendingByEmail(email, EmailVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND));

        if (verification.isExpired()) {
            verification.expire();
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        if (!verification.getVerificationCode().equals(verificationCode)) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        verification.verify();

        String emailVerifyToken = jwtTokenProvider.createEmailVerifyToken(email);

        log.info("이메일 인증 완료. email: {}", email);

        return emailVerifyToken;
    }

    private String generateVerificationCode() {
        SecureRandom random = new SecureRandom();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
