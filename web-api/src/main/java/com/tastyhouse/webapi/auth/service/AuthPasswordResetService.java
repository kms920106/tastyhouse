package com.tastyhouse.webapi.auth.service;

import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.entity.verification.EmailVerification;
import com.tastyhouse.core.entity.verification.EmailVerificationStatus;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.EmailVerificationCoreService;
import com.tastyhouse.external.email.EmailSender;
import com.tastyhouse.webapi.auth.response.PasswordResetTokenResponse;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.member.service.MemberAccountService;
import com.tastyhouse.webapi.verification.VerificationCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthPasswordResetService {

    private static final String EMAIL_SUBJECT = "[TASTY HOUSE] 비밀번호 재설정 인증번호 안내";
    private static final String EMAIL_BODY_TEMPLATE = "[TASTY HOUSE] 비밀번호 재설정 인증번호 [%s]를 입력해주세요. (5분 내 유효)";

    private final MemberQueryService memberQueryService;
    private final EmailVerificationCoreService emailVerificationCoreService;
    private final EmailSender emailSender;
    private final JwtTokenProvider jwtTokenProvider;
    private final VerificationCodeGenerator verificationCodeGenerator;
    private final MemberAccountService memberAccountService;
    private final PasswordEncoder passwordEncoder;

    // 아이디(이메일)로 회원을 조회하고 비밀번호 재설정 인증코드를 발송
    @Transactional
    public void sendPasswordResetCode(String username) {
        // 가입된 회원 여부 확인 (보안상 동일한 응답 반환 — 타이밍 공격 방지를 위해 예외 미노출)
        if (!memberQueryService.existsByUsername(username)) {
            log.info("비밀번호 재설정 요청: 존재하지 않는 아이디. username={}", username);
            return;
        }

        emailVerificationCoreService.expireAllPendingByEmail(username);

        String verificationCode = verificationCodeGenerator.generate();

        emailVerificationCoreService.save(
            EmailVerification.of(
                username,
                verificationCode)
        );

        String emailBody = EMAIL_BODY_TEMPLATE.formatted(verificationCode);
        emailSender.send(username, EMAIL_SUBJECT, emailBody);
    }

    // 인증코드를 검증하고 비밀번호 재설정 토큰을 발급
    @Transactional
    public PasswordResetTokenResponse verifyPasswordResetCode(String username, String verificationCode) {
        EmailVerification verification = emailVerificationCoreService
            .findLatestPendingByEmail(username, EmailVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND));

        if (verification.isExpired()) {
            verification.expire();
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_EXPIRED);
        }

        if (!verification.getVerificationCode().equals(verificationCode)) {
            throw new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_MISMATCH);
        }

        verification.verify();

        String passwordResetToken = jwtTokenProvider.createPasswordResetToken(username);

        return PasswordResetTokenResponse.from(passwordResetToken);
    }

    // 비밀번호 재설정 토큰을 검증하고 새 비밀번호로 변경
    @Transactional
    public void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm) {
        if (!jwtTokenProvider.validatePasswordResetToken(passwordResetToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_RESET_TOKEN_INVALID);
        }

        String username = jwtTokenProvider.getUsernameFromPasswordResetToken(passwordResetToken);

        Member member = memberQueryService.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_SAME_AS_OLD);
        }

        memberAccountService.updatePassword(member.getId(), newPassword, newPasswordConfirm);
    }
}
