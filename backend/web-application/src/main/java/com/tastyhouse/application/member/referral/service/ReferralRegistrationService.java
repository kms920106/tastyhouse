package com.tastyhouse.application.member.referral.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.referral.event.ReferralRegisteredEvent;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.referral.port.out.write.MemberReferralLoadPort;
import com.tastyhouse.application.member.referral.port.out.write.MemberReferralSavePort;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class ReferralRegistrationService {

    private final MemberReferralLoadPort memberReferralLoadPort;
    private final MemberReferralSavePort memberReferralSavePort;
    private final DomainEventPublisher domainEventPublisher;

    public ReferralRegistrationService(
        MemberReferralLoadPort memberReferralLoadPort,
        MemberReferralSavePort memberReferralSavePort,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberReferralLoadPort = memberReferralLoadPort;
        this.memberReferralSavePort = memberReferralSavePort;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void register(MemberId referrerId, MemberId refereeId) {
        if (referrerId.equals(refereeId)) {
            throw new ApplicationException(WebErrorCode.REFERRAL_SELF_NOT_ALLOWED);
        }

        if (memberReferralLoadPort.existsByRefereeId(refereeId)) {
            throw new ApplicationException(WebErrorCode.REFERRAL_ALREADY_EXISTS);
        }

        MemberReferral referral = memberReferralSavePort.save(
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
