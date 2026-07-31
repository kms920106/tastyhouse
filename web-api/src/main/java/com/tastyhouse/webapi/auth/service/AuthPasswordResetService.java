package com.tastyhouse.webapi.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.mail.domain.model.MailVerificationPurpose;
import com.tastyhouse.domain.mail.domain.service.MailVerificationService;
import com.tastyhouse.domain.member.domain.model.Member;
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

    private final MemberRepository memberRepository;
    private final MailVerificationService mailVerificationService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberCommandService memberCommandService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void sendPasswordResetCode(String username) {
        if (!memberRepository.existsByUsername(username)) {
            log.info("비밀번호 재설정 요청: 존재하지 않는 아이디. username={}", username);
            return;
        }

        // 발급이 발송까지 수행한다(문구는 도메인 소유 MailVerificationMessage). 과거에는 이 파사드가
        // MailSender를 직접 주입해 문구 상수와 함께 발송을 호출했는데, 그 구조 때문에 인증코드 발송
        // API 경로는 발송 호출을 빠뜨려 코드가 저장만 되던 버그가 있었다.
        mailVerificationService.issue(username, MailVerificationPurpose.PASSWORD_RESET);
    }

    @Transactional
    public AuthPasswordResetTokenResponse verifyPasswordResetCode(String username, String verificationCode) {
        mailVerificationService.confirm(username, verificationCode);

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
