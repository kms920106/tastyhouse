package com.tastyhouse.webapi.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.model.Member;
import com.tastyhouse.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.domain.verification.domain.port.MailSender;
import com.tastyhouse.domain.verification.domain.service.EmailVerificationService;
import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.webapi.config.jwt.JwtTokenProvider;
import com.tastyhouse.webapi.auth.response.AuthPasswordResetTokenResponse;
import com.tastyhouse.webapi.member.service.MemberCommandService;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthPasswordResetService {

    private static final String EMAIL_SUBJECT = "[TASTY HOUSE] 비밀번호 재설정 인증번호 안내";
    private static final String EMAIL_BODY_TEMPLATE = "[TASTY HOUSE] 비밀번호 재설정 인증번호 [%s]를 입력해주세요. (5분 내 유효)";

    private final MemberRepository memberRepository;
    private final EmailVerificationService emailVerificationService;
    private final MailSender mailSender;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberCommandService memberCommandService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendPasswordResetCode(String username) {
        if (!memberRepository.existsByUsername(username)) {
            log.info("비밀번호 재설정 요청: 존재하지 않는 아이디. username={}", username);
            return;
        }

        EmailVerification verification = emailVerificationService.issue(username);
        String codeValue = verification.getVerificationCode().value();

        String emailBody = EMAIL_BODY_TEMPLATE.formatted(codeValue);
        mailSender.send(username, EMAIL_SUBJECT, emailBody);
    }

    @Transactional
    public AuthPasswordResetTokenResponse verifyPasswordResetCode(String username, String verificationCode) {
        emailVerificationService.confirm(username, verificationCode);

        String passwordResetToken = jwtTokenProvider.createPasswordResetToken(username);

        return AuthPasswordResetTokenResponse.from(passwordResetToken);
    }

    @Transactional
    public void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm) {
        if (!jwtTokenProvider.validatePasswordResetToken(passwordResetToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_RESET_TOKEN_INVALID);
        }

        String username = jwtTokenProvider.getUsernameFromPasswordResetToken(passwordResetToken);

        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_SAME_AS_OLD);
        }

        memberCommandService.updatePassword(member.getId(), newPassword, newPasswordConfirm);
    }
}
