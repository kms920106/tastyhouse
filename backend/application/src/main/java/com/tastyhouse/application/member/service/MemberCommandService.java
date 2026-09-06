package com.tastyhouse.application.member.service;

import com.tastyhouse.application.shared.marker.WebApp;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberSocialAccount;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.repository.MemberSocialAccountRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.member.service.MemberRegistrationService;
import com.tastyhouse.domain.member.service.MemberWithdrawalService;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.application.member.port.in.MemberCommandUseCase;
import com.tastyhouse.application.member.port.in.MemberPasswordUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberPersonalInfoUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberProfileUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberWithdrawCommand;

@Service
@WebApp
@Transactional
public class MemberCommandService implements MemberCommandUseCase {

    private final MemberRepository memberRepository;
    private final MemberSocialAccountRepository memberSocialAccountRepository;
    private final MemberRegistrationService memberRegistrationService;
    private final MemberWithdrawalService memberWithdrawalService;
    private final PasswordEncoder passwordEncoder;

    public MemberCommandService(
        MemberRepository memberRepository,
        MemberSocialAccountRepository memberSocialAccountRepository,
        MemberRegistrationService memberRegistrationService,
        MemberWithdrawalService memberWithdrawalService,
        PasswordEncoder passwordEncoder
    ) {
        this.memberRepository = memberRepository;
        this.memberSocialAccountRepository = memberSocialAccountRepository;
        this.memberRegistrationService = memberRegistrationService;
        this.memberWithdrawalService = memberWithdrawalService;
        this.passwordEncoder = passwordEncoder;
    }

    public Long signUp(
        String username,
        String rawPassword,
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
        return memberRegistrationService.signUp(
            username,
            passwordEncoder.encode(rawPassword),
            nickname,
            fullName,
            gender,
            birthDate,
            phoneNumber,
            pushNotificationEnabled,
            marketingInfoEnabled,
            eventInfoEnabled,
            referrerNickname
        );
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
        return memberRegistrationService.signUpSocial(
            username,
            nickname,
            fullName,
            gender,
            birthDate,
            phoneNumber,
            pushNotificationEnabled,
            marketingInfoEnabled,
            eventInfoEnabled,
            referrerNickname
        );
    }

    @Override
    public void updateProfile(MemberProfileUpdateCommand command) {
        Long profileImageFileId = command.profileImageFileId();
        Member member = loadMember(command.memberId());
        UploadedFileId uploadedFileId = profileImageFileId == null ? null : UploadedFileId.of(profileImageFileId);
        member.updateProfile(command.nickname(), command.statusMessage(), uploadedFileId);
        memberRepository.save(member);
    }

    @Override
    public void updatePersonalInfo(MemberPersonalInfoUpdateCommand command) {
        String gender = command.gender();
        Member member = loadMember(command.memberId());
        member.updatePersonalInfo(
            command.fullName(), command.phoneNumber(), command.birthDate(),
            gender == null ? null : MemberGender.from(gender),
            Boolean.TRUE.equals(command.pushNotificationEnabled()),
            Boolean.TRUE.equals(command.marketingInfoEnabled()),
            Boolean.TRUE.equals(command.eventInfoEnabled())
        );
        memberRepository.save(member);
    }

    @Override
    public void updatePassword(MemberPasswordUpdateCommand command) {
        String newPassword = command.newPassword();
        String newPasswordConfirm = command.newPasswordConfirm();
        Member member = loadMember(command.memberId());

        if (passwordEncoder.matches(newPassword, member.getPassword())) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_SAME_AS_OLD);
        }

        if (!newPassword.equals(newPasswordConfirm)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_CONFIRM_MISMATCH);
        }

        member.updatePassword(passwordEncoder.encode(newPassword));
        memberRepository.save(member);
    }

    @Override
    public void withdraw(MemberWithdrawCommand command) {
        memberWithdrawalService.withdraw(
            MemberId.of(command.memberId()),
            MemberWithdrawalReason.from(command.reason()),
            command.reasonDetail()
        );
    }

    public void saveSocialAccount(MemberSocialAccount socialAccount) {
        memberSocialAccountRepository.save(socialAccount);
    }

    private Member loadMember(Long memberId) {
        return memberRepository.findById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
