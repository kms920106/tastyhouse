package com.tastyhouse.application.auth.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.mail.model.MailVerificationPurpose;
import com.tastyhouse.domain.mail.service.MailVerificationService;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.application.auth.token.MemberJwtTokenProvider;
import com.tastyhouse.application.member.port.in.MemberPasswordUpdateCommand;
import com.tastyhouse.application.member.service.MemberCommandService;

@Service
@WebApp
public class AuthPasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(AuthPasswordResetService.class);

    private final MemberRepository memberRepository;
    private final MailVerificationService mailVerificationService;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final MemberCommandService memberCommandService;

    public AuthPasswordResetService(
        MemberRepository memberRepository,
        MailVerificationService mailVerificationService,
        MemberJwtTokenProvider jwtTokenProvider,
        MemberCommandService memberCommandService
    ) {
        this.memberRepository = memberRepository;
        this.mailVerificationService = mailVerificationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberCommandService = memberCommandService;
    }

    @Transactional
    public void sendPasswordResetCode(String username) {
        if (!memberRepository.existsByUsername(username)) {
            log.info("비밀번호 재설정 요청: 존재하지 않는 아이디. username={}", username);
            return;
        }

        mailVerificationService.issue(username, MailVerificationPurpose.PASSWORD_RESET);
    }

    @Transactional
    public String verifyPasswordResetCode(String username, String verificationCode) {
        mailVerificationService.confirm(username, verificationCode);

        return jwtTokenProvider.createPasswordResetToken(username);
    }

    public void resetPassword(String passwordResetToken, String newPassword, String newPasswordConfirm) {
        if (!jwtTokenProvider.validatePasswordResetToken(passwordResetToken)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_RESET_TOKEN_INVALID);
        }

        String username = jwtTokenProvider.getUsernameFromPasswordResetToken(passwordResetToken);

        Member member = memberRepository.findByUsername(username)
            .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        MemberPasswordUpdateCommand command =
            new MemberPasswordUpdateCommand(member.getId(), newPassword, newPasswordConfirm);
        memberCommandService.updatePassword(command);
    }
}
