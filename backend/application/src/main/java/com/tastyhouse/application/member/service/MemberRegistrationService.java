package com.tastyhouse.application.member.service;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.member.event.MemberRegisteredEvent;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;
import com.tastyhouse.application.member.referral.service.ReferralRegistrationService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.marker.WebApp;

@WebApp
public class MemberRegistrationService {

    private final MemberPersistencePort memberPersistencePort;
    private final ReferralRegistrationService referralRegistrationService;
    private final DomainEventPublisher domainEventPublisher;

    public MemberRegistrationService(
        MemberPersistencePort memberPersistencePort,
        ReferralRegistrationService referralRegistrationService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberPersistencePort = memberPersistencePort;
        this.referralRegistrationService = referralRegistrationService;
        this.domainEventPublisher = domainEventPublisher;
    }

    public Long signUp(
        String username,
        String encodedPassword,
        String nickname,
        String fullName,
        MemberGender gender,
        Integer birthDate,
        String phoneNumber,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        String referrerNickname
    ) {
        if (memberPersistencePort.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.MEMBER_USERNAME_DUPLICATED);
        }
        if (memberPersistencePort.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }
        if (memberPersistencePort.existsByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_ALREADY_REGISTERED);
        }

        Member member = memberPersistencePort.save(Member.of(
            username, encodedPassword, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled
        ));

        registerReferralIfPresent(member, nickname, referrerNickname);
        publishRegistered(member);

        return member.getMemberId().value();
    }

    public Member signUpSocial(
        String username,
        String nickname,
        String fullName,
        MemberGender gender,
        Integer birthDate,
        String phoneNumber,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled,
        String referrerNickname
    ) {
        Member member = memberPersistencePort.save(Member.ofSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled
        ));

        registerReferralIfPresent(member, nickname, referrerNickname);
        publishRegistered(member);

        return member;
    }

    private void registerReferralIfPresent(Member member, String nickname, String referrerNickname) {
        if (referrerNickname == null || referrerNickname.isBlank()) {
            return;
        }
        if (referrerNickname.equals(nickname)) {
            throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
        }

        Member referrer = memberPersistencePort.findByNickname(referrerNickname)
            .orElseThrow(() -> new BusinessException(ErrorCode.REFERRAL_REFERRER_NOT_FOUND));

        referralRegistrationService.register(referrer.getMemberId(), member.getMemberId());
    }

    private void publishRegistered(Member member) {
        domainEventPublisher.publish(new MemberRegisteredEvent(
            member.getMemberId(), member.getUsername(), LocalDateTime.now()
        ));
    }
}
