package com.tastyhouse.webapi.verification;

import com.tastyhouse.core.entity.verification.EmailVerification;
import com.tastyhouse.core.entity.verification.EmailVerificationStatus;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.EmailVerificationCoreService;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.external.email.EmailSender;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private static final String EMAIL_SUBJECT = "[TASTY HOUSE] 이메일 인증번호 안내";
    private static final String EMAIL_BODY_TEMPLATE = "[TASTY HOUSE] 인증번호 [%s]를 입력해주세요. (5분 내 유효)";

    private final MemberQueryService memberQueryService;
    private final EmailVerificationCoreService emailVerificationCoreService;
    private final EmailSender emailSender;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationCodeGenerator verificationCodeGenerator;

    @Transactional
    public void sendVerificationCode(String email) {
        if (memberQueryService.existsByUsername(email)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_REGISTERED);
        }

        emailVerificationCoreService.expireAllPendingByEmail(email);

        String verificationCode = verificationCodeGenerator.generate();

        emailVerificationCoreService.save(
            EmailVerification.of(
                email,
                verificationCode
            )
        );

//        String emailBody = EMAIL_BODY_TEMPLATE.formatted(verificationCode);
//        emailSender.send(email, EMAIL_SUBJECT, emailBody);
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

}
