package com.tastyhouse.domain.mail.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.mail.event.MailVerifiedEvent;
import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationPurpose;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.domain.mail.port.MailSender;
import com.tastyhouse.domain.mail.repository.MailVerificationRepository;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;
import com.tastyhouse.domain.shared.vo.VerificationCode;

public class MailVerificationService {
    private final MemberRepository memberRepository;
    private final MailVerificationRepository mailVerificationRepository;
    private final MailSender mailSender;
    private final DomainEventPublisher domainEventPublisher;

    public MailVerificationService(
        MemberRepository memberRepository,
        MailVerificationRepository mailVerificationRepository,
        MailSender mailSender,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberRepository = memberRepository;
        this.mailVerificationRepository = mailVerificationRepository;
        this.mailSender = mailSender;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void issueForSignUp(String email) {
        if (memberRepository.existsByUsername(email)) {
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_REGISTERED);
        }
        issue(email, MailVerificationPurpose.SIGN_UP);
    }

    public MailVerification issue(String email, MailVerificationPurpose purpose) {
        mailVerificationRepository.expireAllPendingByEmail(email);
        MailVerification saved = mailVerificationRepository.save(MailVerification.create(email));

        mailSender.send(
            email,
            MailVerificationMessage.subject(purpose),
            MailVerificationMessage.body(purpose, saved.getVerificationCode())
        );

        return saved;
    }

    public void confirmForSignUp(String email, String verificationCode) {
        MailVerification verification = confirm(email, verificationCode);

        domainEventPublisher.publish(new MailVerifiedEvent(
            verification.getMailVerificationId(),
            email,
            verification.getVerifiedAt()
        ));
    }

    public MailVerification confirm(String email, String verificationCode) {
        MailVerification verification = mailVerificationRepository
            .findLatestPendingByEmail(email, MailVerificationStatus.PENDING)
            .orElseThrow(() -> new BusinessException(ErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND));

        verification.verify(VerificationCode.of(verificationCode), LocalDateTime.now());
        return mailVerificationRepository.save(verification);
    }
}
