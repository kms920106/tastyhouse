package com.tastyhouse.application.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.mail.model.MailVerificationPurpose;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.application.auth.token.MemberJwtTokenProvider;
import com.tastyhouse.application.mail.service.MailVerificationService;
import com.tastyhouse.application.member.port.in.MemberCommandUseCase;
import com.tastyhouse.application.member.port.in.MemberPasswordUpdateCommand;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class AuthPasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(AuthPasswordResetService.class);

    private final MemberPersistencePort memberPersistencePort;
    private final MailVerificationService mailVerificationService;
    private final MemberJwtTokenProvider jwtTokenProvider;
    private final MemberCommandUseCase memberCommandUseCase;

    public AuthPasswordResetService(
        MemberPersistencePort memberPersistencePort,
        MailVerificationService mailVerificationService,
        MemberJwtTokenProvider jwtTokenProvider,
        MemberCommandUseCase memberCommandUseCase
    ) {
        this.memberPersistencePort = memberPersistencePort;
        this.mailVerificationService = mailVerificationService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.memberCommandUseCase = memberCommandUseCase;
    }

    @Transactional
    public void sendPasswordResetCode(String username) {
        if (!memberPersistencePort.existsByUsername(username)) {
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
            throw new ApplicationException(WebErrorCode.MEMBER_PASSWORD_RESET_TOKEN_INVALID);
        }

        String username = jwtTokenProvider.getUsernameFromPasswordResetToken(passwordResetToken);

        Member member = memberPersistencePort.findByUsername(username)
            .orElseThrow(() -> new ApplicationException(ApplicationErrorCode.MEMBER_NOT_FOUND));

        MemberPasswordUpdateCommand command =
            new MemberPasswordUpdateCommand(member.getId(), newPassword, newPasswordConfirm);
        memberCommandUseCase.updatePassword(command);
    }
}
