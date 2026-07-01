package com.tastyhouse.core.domain.verification.application;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.verification.application.dto.command.ConfirmEmailVerificationCommand;
import com.tastyhouse.core.domain.verification.application.dto.command.SendEmailVerificationCommand;
import com.tastyhouse.core.domain.verification.application.dto.result.EmailVerificationResult;
import com.tastyhouse.core.domain.verification.domain.event.EmailVerifiedEvent;
import com.tastyhouse.core.domain.verification.domain.model.EmailVerification;
import com.tastyhouse.core.domain.verification.domain.model.EmailVerificationStatus;
import com.tastyhouse.core.domain.verification.domain.repository.EmailVerificationRepository;
import com.tastyhouse.core.domain.verification.domain.vo.VerificationCode;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationCommandService {

    private final MemberQueryService memberQueryService;
    private final EmailVerificationRepository emailVerificationRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void sendVerificationCode(SendEmailVerificationCommand command) {
        if (memberQueryService.existsByUsername(command.email())) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_REGISTERED);
        }

        emailVerificationRepository.expireAllPendingByEmail(command.email());

        emailVerificationRepository.save(EmailVerification.create(command.email()));

//        mailSender.send(command.email(), EMAIL_SUBJECT, EMAIL_BODY_TEMPLATE.formatted(verificationCode));
    }

    @Transactional
    public EmailVerificationResult confirmVerificationCode(ConfirmEmailVerificationCommand command) {
        EmailVerification verification = emailVerificationRepository
            .findLatestPendingByEmail(command.email(), EmailVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();
        verification.verify(VerificationCode.of(command.verificationCode()), now);

        eventPublisher.publishEvent(new EmailVerifiedEvent(
            verification.getEmailVerificationId(),
            command.email(),
            now
        ));

        log.info("이메일 인증 완료. email={}", command.email());

        return new EmailVerificationResult(command.email());
    }
}
