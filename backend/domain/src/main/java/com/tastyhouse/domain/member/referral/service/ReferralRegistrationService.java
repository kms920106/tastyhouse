package com.tastyhouse.domain.member.referral.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.referral.event.ReferralRegisteredEvent;
import com.tastyhouse.domain.member.referral.model.MemberReferral;
import com.tastyhouse.domain.member.referral.repository.MemberReferralRepository;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.event.DomainEventPublisher;

public class ReferralRegistrationService {
    private final MemberReferralRepository memberReferralRepository;
    private final DomainEventPublisher domainEventPublisher;

    public ReferralRegistrationService(
        MemberReferralRepository memberReferralRepository,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberReferralRepository = memberReferralRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void register(MemberId referrerId, MemberId refereeId) {
        if (referrerId.equals(refereeId)) {
            throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
        }

        if (memberReferralRepository.existsByRefereeId(refereeId)) {
            throw new BusinessException(ErrorCode.REFERRAL_ALREADY_EXISTS);
        }

        MemberReferral referral = memberReferralRepository.save(
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
