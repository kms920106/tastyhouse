package com.tastyhouse.webapi.member.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberGender;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.model.MemberWithdrawalReason;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.member.application.MemberCommandService;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.command.PersonalInfoUpdateCommand;
import com.tastyhouse.core.domain.member.application.dto.command.ProfileUpdateCommand;
import com.tastyhouse.core.domain.member.application.dto.command.WithdrawMemberCommand;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.response.MyProfileResponse;
import com.tastyhouse.webapi.member.response.NicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.response.PersonalInfoResponse;
import com.tastyhouse.webapi.member.response.PhoneAvailabilityResponse;


@Service
@RequiredArgsConstructor
public class MemberAccountService {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 (토큰 검증은 MemberFacade에서 MemberAuthService를 통해 선행)
    @Transactional
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
        memberCommandService.signUp(
            username,
            passwordEncoder.encode(password),
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

    // 새 비밀번호 확인 일치 여부를 검증한 후 변경 (기존 비번 동일 여부는 MemberFacade에서 MemberAuthService를 통해 선행)
    @Transactional
    public void updatePassword(
        Long memberId,
        String newPassword,
        String newPasswordConfirm
    ) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new com.tastyhouse.core.exception.BusinessException(ErrorCode.MEMBER_PASSWORD_CONFIRM_MISMATCH);
        }

        memberCommandService.updatePassword(
            MemberId.of(memberId),
            passwordEncoder.encode(newPassword)
        );
    }

    // 회원을 비활성화하고 탈퇴 사유를 저장
    @Transactional
    public void withdrawMember(
        Long memberId,
        MemberWithdrawalReason reason,
        String reasonDetail
    ) {
        memberCommandService.withdraw(
            WithdrawMemberCommand.of(MemberId.of(memberId), reason, reasonDetail)
        );
    }

    // 닉네임 중복 여부를 확인하여 사용 가능 여부를 반환
    @Transactional(readOnly = true)
    public NicknameAvailabilityResponse checkNicknameAvailability(String nickname) {
        boolean available = !memberQueryService.existsByNickname(nickname);
        return NicknameAvailabilityResponse.from(available);
    }

    // 휴대폰번호로 활성 회원 존재 여부를 확인하여 가입 가능 여부를 반환
    @Transactional(readOnly = true)
    public PhoneAvailabilityResponse checkPhoneAvailability(String phoneNumber) {
        boolean available = !memberQueryService.existsByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);
        return PhoneAvailabilityResponse.from(available);
    }

    // 회원의 이름, 휴대폰, 생년월일, 성별, 알림 수신 설정을 수정
    @Transactional
    public void updatePersonalInfo(
        Long memberId,
        String fullName,
        String phoneNumber,
        Integer birthDate,
        MemberGender gender,
        boolean pushNotificationEnabled,
        boolean marketingInfoEnabled,
        boolean eventInfoEnabled
    ) {
        memberCommandService.updatePersonalInfo(
            PersonalInfoUpdateCommand.of(
                MemberId.of(memberId),
                fullName,
                phoneNumber,
                birthDate,
                gender,
                pushNotificationEnabled,
                marketingInfoEnabled,
                eventInfoEnabled
            )
        );
    }

    // 회원의 프로필 조회
    @Transactional(readOnly = true)
    public MemberProfileResponse getMemberProfile(Long targetMemberId) {
        MemberWithProfileImageResult result = memberQueryService.findMemberWithProfileImage(MemberId.of(targetMemberId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberProfileResponse.from(
            result.nickname(),
            result.memberGrade().name(),
            result.statusMessage(),
            fileService.getUrlByPath(result.profileImageFilePath())
        );
    }

    // 로그인한 회원 본인의 프로필 조회 (소유권 비교용 식별자 id 포함)
    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long memberId) {
        MemberWithProfileImageResult result = memberQueryService.findMemberWithProfileImage(MemberId.of(memberId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return MyProfileResponse.from(
            memberId,
            result.nickname(),
            result.memberGrade().name(),
            result.statusMessage(),
            fileService.getUrlByPath(result.profileImageFilePath())
        );
    }

    // 회원의 닉네임, 상태 메시지, 프로필 이미지를 수정
    @Transactional
    public void updateMemberProfile(
        Long memberId,
        String nickname,
        String statusMessage,
        Long profileImageFileId
    ) {
        memberCommandService.updateProfile(
            ProfileUpdateCommand.of(
                MemberId.of(memberId),
                nickname,
                statusMessage,
                profileImageFileId
            )
        );
    }

    // 회원의 개인정보를 조회하여 반환
    @Transactional(readOnly = true)
    public PersonalInfoResponse getPersonalInfo(Long memberId) {
        Member member = memberQueryService.getById(MemberId.of(memberId));
        return PersonalInfoResponse.of(
            member.getUsername(),
            member.getFullName(),
            member.getPhoneNumber().getValue(),
            member.getBirthDate(),
            member.getGender().name(),
            member.isPushNotificationEnabled(),
            member.isMarketingInfoEnabled(),
            member.isEventInfoEnabled()
        );
    }
}
