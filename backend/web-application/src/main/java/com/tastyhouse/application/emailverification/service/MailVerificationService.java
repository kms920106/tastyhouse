package com.tastyhouse.application.emailverification.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.emailverification.event.MailVerifiedEvent;
import com.tastyhouse.domain.emailverification.model.MailVerification;
import com.tastyhouse.domain.emailverification.model.MailVerificationPurpose;
import com.tastyhouse.domain.emailverification.model.MailVerificationStatus;
import com.tastyhouse.domain.shared.vo.VerificationCode;
import com.tastyhouse.application.emailverification.port.out.MailSendResult;
import com.tastyhouse.application.emailverification.port.out.MailSenderPort;
import com.tastyhouse.application.emailverification.port.out.write.MailVerificationLoadPort;
import com.tastyhouse.application.emailverification.port.out.write.MailVerificationSavePort;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class MailVerificationService {

    private final MemberLoadPort memberLoadPort;
    private final MailVerificationLoadPort mailVerificationLoadPort;
    private final MailVerificationSavePort mailVerificationSavePort;
    private final MailSenderPort mailSender;
    private final DomainEventPublisher domainEventPublisher;

    public MailVerificationService(
        MemberLoadPort memberLoadPort,
        MailVerificationLoadPort mailVerificationLoadPort,
        MailVerificationSavePort mailVerificationSavePort,
        MailSenderPort mailSender,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberLoadPort = memberLoadPort;
        this.mailVerificationLoadPort = mailVerificationLoadPort;
        this.mailVerificationSavePort = mailVerificationSavePort;
        this.mailSender = mailSender;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void issueForSignUp(String email) {
        if (memberLoadPort.existsByUsername(email)) {
            throw new ApplicationException(WebErrorCode.MEMBER_EMAIL_ALREADY_REGISTERED);
        }
        issue(email, MailVerificationPurpose.SIGN_UP);
    }

    public MailVerification issue(String email, MailVerificationPurpose purpose) {
        mailVerificationSavePort.expireAllPendingByEmail(email);
        MailVerification saved = mailVerificationSavePort.save(MailVerification.create(email));

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
        MailVerification verification = mailVerificationLoadPort
            .findLatestPendingByEmail(email, MailVerificationStatus.PENDING)
            .orElseThrow(() -> new ApplicationException(WebErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND));

        verification.verify(VerificationCode.of(verificationCode), LocalDateTime.now());
        return mailVerificationSavePort.save(verification);
    }
}
