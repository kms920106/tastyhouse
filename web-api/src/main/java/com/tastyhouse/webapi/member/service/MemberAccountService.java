package com.tastyhouse.webapi.member.service;

import com.tastyhouse.core.entity.referral.MemberReferral;
import com.tastyhouse.core.entity.user.Member;
import com.tastyhouse.core.entity.user.WithdrawalReason;
import com.tastyhouse.core.entity.user.MemberWithdrawal;
import com.tastyhouse.core.entity.user.MemberStatus;
import com.tastyhouse.core.entity.user.Gender;
import com.tastyhouse.core.exception.BusinessException;
import com.tastyhouse.core.exception.ErrorCode;
import com.tastyhouse.core.service.MemberCoreService;
import com.tastyhouse.external.file.FileService;
import com.tastyhouse.webapi.member.response.NicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.response.PersonalInfoResponse;
import com.tastyhouse.webapi.member.response.PhoneAvailabilityResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
@RequiredArgsConstructor
public class MemberAccountService {

    private final MemberCoreService memberCoreService;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    // 회원가입 (토큰 검증은 MemberFacade에서 MemberAuthService를 통해 선행)
    @Transactional
    public void signUp(String username, String password,
                       String nickname, String fullName,
                       Gender gender,
                       Integer birthDate, String phoneNumber,
                       Boolean pushNotificationEnabled,
                       Boolean marketingInfoEnabled, Boolean eventInfoEnabled,
                       String referrerNickname) {

        if (memberCoreService.existsByUsername(username)) {
            throw new BusinessException(ErrorCode.MEMBER_USERNAME_DUPLICATED);
        }

        if (memberCoreService.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.MEMBER_NICKNAME_DUPLICATED);
        }

        if (memberCoreService.existsByPhoneNumberValueAndMemberStatusNot(phoneNumber, MemberStatus.DELETED)) {
            throw new BusinessException(ErrorCode.MEMBER_PHONE_ALREADY_REGISTERED);
        }

        Member member = new Member(username, passwordEncoder.encode(password), nickname, fullName, gender, birthDate, phoneNumber, pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
        memberCoreService.save(member);

        if (StringUtils.hasText(referrerNickname)) {
            if (referrerNickname.equals(nickname)) {
                throw new BusinessException(ErrorCode.REFERRAL_SELF_NOT_ALLOWED);
            }

            Member referrer = memberCoreService.findByNickname(referrerNickname)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFERRAL_REFERRER_NOT_FOUND));

            memberCoreService.saveReferral(
                MemberReferral.of(
                    referrer.getId(),
                    member.getId())
            );
        }
    }

    // 새 비밀번호 확인 일치 여부를 검증한 후 변경 (기존 비번 동일 여부는 MemberFacade에서 MemberAuthService를 통해 선행)
    @Transactional
    public void updatePassword(Long memberId, String newPassword, String newPasswordConfirm) {
        if (!newPassword.equals(newPasswordConfirm)) {
            throw new BusinessException(ErrorCode.MEMBER_PASSWORD_CONFIRM_MISMATCH);
        }

        Member member = memberCoreService.getById(memberId);
        member.changePassword(passwordEncoder.encode(newPassword));
    }

    // 회원을 비활성화하고 탈퇴 사유를 저장
    @Transactional
    public void withdrawMember(Long memberId, WithdrawalReason reason, String reasonDetail) {
        memberCoreService.getById(memberId).deactivate();

        memberCoreService.saveWithdrawal(
            MemberWithdrawal.of(
                memberId,
                reason,
                reasonDetail
            )
        );
    }

    // 닉네임 중복 여부를 확인하여 사용 가능 여부를 반환
    @Transactional(readOnly = true)
    public NicknameAvailabilityResponse checkNicknameAvailability(String nickname) {
        boolean available = !memberCoreService.existsByNickname(nickname);
        return NicknameAvailabilityResponse.from(available);
    }

    // 휴대폰번호로 활성 회원 존재 여부를 확인하여 가입 가능 여부를 반환
    @Transactional(readOnly = true)
    public PhoneAvailabilityResponse checkPhoneAvailability(String phoneNumber) {
        boolean available = !memberCoreService.existsByPhoneNumberValueAndMemberStatusNot(
            phoneNumber, MemberStatus.DELETED
        );
        return PhoneAvailabilityResponse.from(available);
    }

    // 회원의 이름, 휴대폰, 생년월일, 성별, 알림 수신 설정을 수정
    @Transactional
    public void updatePersonalInfo(Long memberId, String fullName, String phoneNumber, Integer birthDate,
                                   Gender gender,
                                   Boolean pushNotificationEnabled, Boolean marketingInfoEnabled,
                                   Boolean eventInfoEnabled) {
        memberCoreService.getById(memberId).updatePersonalInfo(fullName, phoneNumber, birthDate, gender, pushNotificationEnabled, marketingInfoEnabled, eventInfoEnabled);
    }

    // 회원의 닉네임, 상태 메시지, 프로필 이미지를 수정
    @Transactional
    public void updateMemberProfile(Long memberId, String nickname, String statusMessage, Long profileImageFileId) {
        memberCoreService.getById(memberId).changeProfile(nickname, statusMessage, profileImageFileId);
    }

    // 회원의 개인정보를 조회하여 반환
    @Transactional(readOnly = true)
    public PersonalInfoResponse getPersonalInfo(Long memberId) {
        Member member = memberCoreService.getById(memberId);
        return PersonalInfoResponse.from(member);
    }
}
