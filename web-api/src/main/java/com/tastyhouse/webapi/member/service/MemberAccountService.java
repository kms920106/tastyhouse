package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.domain.member.application.MemberCommandService;
import com.tastyhouse.core.domain.member.application.MemberQueryService;
import com.tastyhouse.core.domain.member.application.dto.command.UpdatePersonalInfoCommand;
import com.tastyhouse.core.domain.member.application.dto.command.UpdateProfileCommand;
import com.tastyhouse.core.domain.member.application.dto.command.WithdrawMemberCommand;
import com.tastyhouse.core.domain.member.application.dto.result.MemberWithProfileImageResult;
import com.tastyhouse.core.domain.member.domain.model.Gender;
import com.tastyhouse.core.domain.member.domain.model.Member;
import com.tastyhouse.core.domain.member.domain.model.MemberStatus;
import com.tastyhouse.core.domain.member.domain.model.WithdrawalReason;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.exception.EntityNotFoundException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.response.NicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.response.PersonalInfoResponse;
import com.tastyhouse.webapi.member.response.PhoneAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
        Gender gender,
        Integer birthDate,
        String phoneNumber,
        Boolean pushNotificationEnabled,
        Boolean marketingInfoEnabled,
        Boolean eventInfoEnabled,
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
            new MemberId(memberId),
            passwordEncoder.encode(newPassword)
        );
    }

    // 회원을 비활성화하고 탈퇴 사유를 저장
    @Transactional
    public void withdrawMember(
        Long memberId,
        WithdrawalReason reason,
        String reasonDetail
    ) {
        memberCommandService.withdraw(
            new WithdrawMemberCommand(new MemberId(memberId), reason, reasonDetail)
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
        Gender gender,
        Boolean pushNotificationEnabled,
        Boolean marketingInfoEnabled,
        Boolean eventInfoEnabled
    ) {
        memberCommandService.updatePersonalInfo(
            new UpdatePersonalInfoCommand(
                new MemberId(memberId),
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
        MemberWithProfileImageResult result = memberQueryService.findMemberWithProfileImage(new MemberId(targetMemberId))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return MemberProfileResponse.from(
            result.nickname(),
            result.memberGrade(),
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
            new UpdateProfileCommand(
                new MemberId(memberId),
                nickname,
                statusMessage,
                profileImageFileId
            )
        );
    }

    // 회원의 개인정보를 조회하여 반환
    @Transactional(readOnly = true)
    public PersonalInfoResponse getPersonalInfo(Long memberId) {
        Member member = memberQueryService.getById(new MemberId(memberId));
        return PersonalInfoResponse.from(member);
    }
}
