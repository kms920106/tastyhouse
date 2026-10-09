package com.tastyhouse.application.member.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.tastyhouse.domain.member.event.MemberRegisteredEvent;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.application.member.port.out.write.MemberLoadPort;
import com.tastyhouse.application.member.port.out.write.MemberSavePort;
import com.tastyhouse.application.member.referral.service.ReferralRegistrationService;
import com.tastyhouse.application.shared.event.DomainEventPublisher;
import com.tastyhouse.application.shared.exception.ApplicationException;
import com.tastyhouse.application.shared.exception.WebErrorCode;

@Service
public class MemberRegistrationService {

    private final MemberLoadPort memberLoadPort;
    private final MemberSavePort memberSavePort;
    private final ReferralRegistrationService referralRegistrationService;
    private final DomainEventPublisher domainEventPublisher;

    public MemberRegistrationService(
        MemberLoadPort memberLoadPort,
        MemberSavePort memberSavePort,
        ReferralRegistrationService referralRegistrationService,
        DomainEventPublisher domainEventPublisher
    ) {
        this.memberLoadPort = memberLoadPort;
        this.memberSavePort = memberSavePort;
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
        if (memberLoadPort.existsByUsername(username)) {
            throw new ApplicationException(WebErrorCode.MEMBER_USERNAME_DUPLICATED);
        }
        if (memberLoadPort.existsByNickname(nickname)) {
            throw new ApplicationException(WebErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }
        if (memberLoadPort.existsByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED)) {
            throw new ApplicationException(WebErrorCode.MEMBER_PHONE_ALREADY_REGISTERED);
        }

        Member member = memberSavePort.save(Member.of(
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
        Member member = memberSavePort.save(Member.ofSocial(
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
            throw new ApplicationException(WebErrorCode.REFERRAL_SELF_NOT_ALLOWED);
        }

        Member referrer = memberLoadPort.findByNickname(referrerNickname)
            .orElseThrow(() -> new ApplicationException(WebErrorCode.REFERRAL_REFERRER_NOT_FOUND));

        referralRegistrationService.register(referrer.getMemberId(), member.getMemberId());
    }

    private void publishRegistered(Member member) {
        domainEventPublisher.publish(new MemberRegisteredEvent(
            member.getMemberId(), member.getUsername(), LocalDateTime.now()
        ));
    }
}
