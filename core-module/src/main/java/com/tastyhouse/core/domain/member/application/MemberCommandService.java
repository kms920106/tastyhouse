package com.tastyhouse.core.domain.member.application;

import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.member.domain.event.MemberRegisteredEvent;
import com.tastyhouse.core.domain.member.domain.event.MemberWithdrawnEvent;
import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberGender;
import com.tastyhouse.core.domain.member.domain.model.MemberGrade;
import com.tastyhouse.core.domain.member.domain.model.MemberSocialAccount;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.model.MemberWithdrawal;
import com.tastyhouse.core.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.core.domain.member.domain.repository.MemberSocialAccountRepository;
import com.tastyhouse.core.domain.member.domain.repository.MemberWithdrawalRepository;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.application.dto.command.PersonalInfoUpdateCommand;
import com.tastyhouse.core.domain.member.application.dto.command.ProfileUpdateCommand;
import com.tastyhouse.core.domain.member.application.dto.command.WithdrawMemberCommand;
import com.tastyhouse.core.domain.member.referral.application.ReferralCommandService;
import com.tastyhouse.core.domain.member.referral.application.dto.command.RegisterReferralCommand;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberCommandService {

    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final MemberWithdrawalRepository memberWithdrawalRepository;
    private final ReferralCommandService referralCommandService;
    private final ApplicationEventPublisher eventPublisher;

    public void signUp(
        String username,
        String password,
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
        if (memberRepository.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.MEMBER_USERNAME_DUPLICATED);
        }
        if (memberRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }
        if (memberRepository.existsByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_ALREADY_REGISTERED);
        }

        Member member = memberRepository.save(Member.of(
            username, password, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled
        ));

        if (StringUtils.hasText(referrerNickname)) {
            if (referrerNickname.equals(nickname)) {
                throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
            }
            Member referrer = memberRepository.findByNickname(referrerNickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFERRAL_REFERRER_NOT_FOUND));
            RegisterReferralCommand command = RegisterReferralCommand.of(referrer.getMemberId(), member.getMemberId());
            referralCommandService.register(command);
        }

        eventPublisher.publishEvent(new MemberRegisteredEvent(
            member.getMemberId(), member.getUsername(), LocalDateTime.now()
        ));
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
        Member member = memberRepository.save(Member.ofSocial(
            username, nickname, fullName, gender, birthDate, phoneNumber,
            pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled
        ));

        if (StringUtils.hasText(referrerNickname)) {
            if (referrerNickname.equals(nickname)) {
                throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
            }
            Member referrer = memberRepository.findByNickname(referrerNickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFERRAL_REFERRER_NOT_FOUND));
            RegisterReferralCommand command = RegisterReferralCommand.of(referrer.getMemberId(), member.getMemberId());
            referralCommandService.register(command);
        }

        eventPublisher.publishEvent(new MemberRegisteredEvent(
            member.getMemberId(), member.getUsername(), LocalDateTime.now()
        ));

        return member;
    }

    public void withdraw(WithdrawMemberCommand command) {
        Member member = memberRepository.findById(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        member.withdraw();
        memberRepository.save(member);

        LocalDateTime now = LocalDateTime.now();
        memberWithdrawalRepository.save(
            MemberWithdrawal.of(command.memberId(), command.reason(), command.reasonDetail())
        );

        eventPublisher.publishEvent(
            new MemberWithdrawnEvent(member.getMemberId(), command.reason(), now)
        );
    }

    public void updateProfile(ProfileUpdateCommand command) {
        Member member = memberRepository.findById(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        member.updateProfile(command.nickname(), command.statusMessage(), command.profileImageFileId());
    }

    public void updatePersonalInfo(PersonalInfoUpdateCommand command) {
        Member member = memberRepository.findById(command.memberId())
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        member.updatePersonalInfo(
            command.fullName(), command.phoneNumber(), command.birthDate(), command.gender(),
            command.pushNotificationEnabled(), command.marketingInfoEnabled(), command.eventInfoEnabled()
        );
    }

    public void updatePassword(MemberId memberId, String encodedPassword) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        member.updatePassword(encodedPassword);
    }

    public long bulkUpdateGrade(List<Long> memberIds, MemberGrade grade) {
        return memberRepository.bulkUpdateGrade(memberIds, grade);
    }

    public void saveSocialAccount(MemberSocialAccount socialAccount) {
        memberSocialAccountRepository.save(socialAccount);
    }
}
