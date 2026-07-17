package com.tastyhouse.webapi.auth.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.core.domain.verification.domain.model.EmailVerificationStatus;
import com.tastyhouse.core.domain.verification.domain.repository.EmailVerificationRepository;
import com.tastyhouse.core.domain.verification.domain.vo.VerificationCode;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.verification.application.port.out.MailSender;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.auth.response.AuthPasswordResetTokenResponse;
import com.tastyhouse.webapi.member.service.MemberAccountService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthPasswordResetService {

    private static final String EMAIL_SUBJECT = "[TASTY HOUSE] 비밀번호 재설정 인증번호 안내";
    private static final String EMAIL_BODY_TEMPLATE = "[TASTY HOUSE] 비밀번호 재설정 인증번호 [%s]를 입력해주세요. (5분 내 유효)";

    private final MemberQueryService memberQueryService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MailSender mailSender;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberAccountService memberAccountService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendPasswordResetCode(String username) {
        if (!memberQueryService.existsByUsername(username)) {
            log.info("비밀번호 재설정 요청: 존재하지 않는 아이디. username={}", username);
            return;
        }

        emailVerificationRepository.expireAllPendingByEmail(username);

        EmailVerification verification = EmailVerification.create(username);
        String codeValue = verification.getVerificationCode().getValue();

        emailVerificationRepository.save(verification);

        String emailBody = EMAIL_BODY_TEMPLATE.formatted(codeValue);
        mailSender.send(username, EMAIL_SUBJECT, emailBody);
    }

    @Transactional
    public AuthPasswordResetTokenResponse verifyPasswordResetCode(String username, String verificationCode) {
        EmailVerification verification = emailVerificationRepository
            .findLatestPendingByEmail(username, EmailVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND));

        verification.verify(VerificationCode.of(verificationCode), LocalDateTime.now());

        String passwordResetToken = jwtTokenProvider.createPasswordResetToken(username);

        return AuthPasswordResetTokenResponse.from(passwordResetToken);
    }

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
