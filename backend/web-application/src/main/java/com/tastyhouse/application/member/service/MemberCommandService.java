package com.tastyhouse.application.member.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberGender;
import com.tastyhouse.domain.member.model.MemberWithdrawalReason;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.in.MemberCommandUseCase;
import com.tastyhouse.application.member.port.in.MemberPasswordUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberPersonalInfoUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberProfileUpdateCommand;
import com.tastyhouse.application.member.port.in.MemberWithdrawCommand;
import com.tastyhouse.application.member.port.out.write.MemberPersistencePort;

@Service
@Transactional
class MemberCommandService implements MemberCommandUseCase {

    private final MemberPersistencePort memberPersistencePort;
    private final MemberWithdrawalService memberWithdrawalService;
    private final PasswordEncoder passwordEncoder;

    public MemberCommandService(
        MemberPersistencePort memberPersistencePort,
        MemberWithdrawalService memberWithdrawalService,
        PasswordEncoder passwordEncoder
    ) {
        this.memberPersistencePort = memberPersistencePort;
        this.memberWithdrawalService = memberWithdrawalService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void updateProfile(MemberProfileUpdateCommand command) {
        Long profileImageFileId = command.profileImageFileId();
        Member member = loadMember(command.memberId());
        UploadedFileId uploadedFileId = profileImageFileId == null ? null : UploadedFileId.of(profileImageFileId);
        member.updateProfile(command.nickname(), command.statusMessage(), uploadedFileId);
        memberPersistencePort.save(member);
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
        memberPersistencePort.save(member);
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
        memberPersistencePort.save(member);
    }

    @Override
    public void withdraw(MemberWithdrawCommand command) {
        memberWithdrawalService.withdraw(
            MemberId.of(command.memberId()),
            MemberWithdrawalReason.from(command.reason()),
            command.reasonDetail()
        );
    }

    private Member loadMember(Long memberId) {
        return memberPersistencePort.findById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
