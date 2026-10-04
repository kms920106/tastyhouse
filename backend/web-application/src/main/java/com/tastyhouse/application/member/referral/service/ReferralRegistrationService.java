package com.tastyhouse.application.member.referral.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.referral.event.ReferralRegisteredEvent;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.referral.port.out.write.MemberReferralPersistencePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class ReferralRegistrationService {

    private final MemberReferralPersistencePort memberReferralPersistencePort;
    private final DomainEventPublisher domainEventPublisher;

    public ReferralRegistrationService(
        MemberReferralPersistencePort memberReferralPersistencePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberReferralPersistencePort = memberReferralPersistencePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void register(MemberId referrerId, MemberId refereeId) {
        if (referrerId.equals(refereeId)) {
            throw new ApplicationException(WebErrorCode.REFERRAL_SELF_NOT_ALLOWED);
        }

        if (memberReferralPersistencePort.existsByRefereeId(refereeId)) {
            throw new ApplicationException(WebErrorCode.REFERRAL_ALREADY_EXISTS);
        }

        MemberReferral referral = memberReferralPersistencePort.save(
            MemberReferral.register(referrerId, refereeId)
        );

        domainEventPublisher.publish(new ReferralRegisteredEvent(
            referral.getReferralId(),
            referral.getReferrerId(),
            referral.getRefereeId(),
            LocalDateTime.now()
        ));
    }
}
