package com.tastyhouse.application.mail.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.mail.event.MailVerifiedEvent;
import com.tastyhouse.domain.mail.model.MailVerification;
import com.tastyhouse.domain.mail.model.MailVerificationPurpose;
import com.tastyhouse.domain.mail.model.MailVerificationStatus;
import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.application.mail.port.out.MailSendResult;
import com.tastyhouse.application.mail.port.out.MailSender;
import com.tastyhouse.application.mail.port.out.write.MailVerificationPersistencePort;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class MailVerificationService {

    private final MemberPersistencePort memberPersistencePort;
    private final MailVerificationPersistencePort mailVerificationPersistencePort;
    private final MailSender mailSender;
    private final DomainEventPublisher domainEventPublisher;

    public MailVerificationService(
        MemberPersistencePort memberPersistencePort,
        MailVerificationPersistencePort mailVerificationPersistencePort,
        MailSender mailSender,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberPersistencePort = memberPersistencePort;
        this.mailVerificationPersistencePort = mailVerificationPersistencePort;
        this.mailSender = mailSender;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void issueForSignUp(String email) {
        if (memberPersistencePort.existsByUsername(email)) {
            throw new ApplicationException(WebErrorCode.MEMBER_EMAIL_ALREADY_REGISTERED);
        }
        issue(email, MailVerificationPurpose.SIGN_UP);
    }

    public MailVerification issue(String email, MailVerificationPurpose purpose) {
        mailVerificationPersistencePort.expireAllPendingByEmail(email);
        MailVerification saved = mailVerificationPersistencePort.save(MailVerification.create(email));

        MailSendResult result = mailSender.send(
            email,
            MailVerificationMessage.subject(purpose),
            MailVerificationMessage.body(purpose, saved.getVerificationCode())
        );
        if (!result.success()) {
            throw new ApplicationException(WebErrorCode.MAIL_SEND_FAILED, result.cause());
        }

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
        MailVerification verification = mailVerificationPersistencePort
            .findLatestPendingByEmail(email, MailVerificationStatus.PENDING)
            .orElseThrow(() -> new ApplicationException(WebErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND));

        verification.verify(VerificationCode.of(verificationCode), LocalDateTime.now());
        return mailVerificationPersistencePort.save(verification);
    }
}
