package com.tastyhouse.webapplication.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.webapplication.member.response.MemberNicknameAvailabilityResponse;
import com.tastyhouse.webapplication.member.response.MemberPersonalInfoResponse;
import com.tastyhouse.webapplication.member.response.MemberPhoneAvailabilityResponse;
import com.tastyhouse.webapplication.member.response.MemberProfileResponse;
import com.tastyhouse.webapplication.member.response.MyProfileResponse;
import com.tastyhouse.webapplication.member.port.in.MemberQueryUseCase;

/**
 * 회원 조회 서비스.
 *
 * <p>프로필 카드·개인정보 조회와 닉네임·휴대폰 사용 가능 여부까지 모두 읽기 포트
 * ({@link MemberQueryPort})의 투영으로 답한다 — 전부 표현 목적 조회라 write 포트를 주입하지 않는다
 * (CQRS 교차 주입 금지).
 *
 * <p>프로필 이미지는 DAO가 표시용 URL까지 변환해 담으므로, 이 서비스는 그 값을 그대로 응답에 전달한다.
 */
@Service
@Transactional(readOnly = true)
public class MemberQueryService implements MemberQueryUseCase {

    private final MemberQueryPort memberQueryPort;

    public MemberQueryService(MemberQueryPort memberQueryPort) {
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public MemberNicknameAvailabilityResponse checkNicknameAvailability(String nickname) {
        boolean available = !memberQueryPort.existsByNickname(nickname);
        return MemberNicknameAvailabilityResponse.from(available);
    }

    @Override
    public MemberPhoneAvailabilityResponse checkPhoneAvailability(String phoneNumber) {
        boolean available = !memberQueryPort.existsByActivePhoneNumber(phoneNumber);
        return MemberPhoneAvailabilityResponse.from(available);
    }

    /** 타 회원 프로필 조회 — 식별자는 노출하지 않는다. */
    @Override
    public MemberProfileResponse getMemberProfile(Long targetMemberId) {
        MemberWithProfileImageResult result = findProfile(targetMemberId);
        return MemberProfileResponse.from(
            result.nickname(),
            result.memberGrade().name(),
            result.statusMessage(),
            result.profileImageUrl()
        );
    }

    /** 본인 프로필 조회 — 소유권 비교용 식별자를 함께 내보낸다. */
    @Override
    public MyProfileResponse getMyProfile(Long memberId) {
        MemberWithProfileImageResult result = findProfile(memberId);
        return MyProfileResponse.from(
            memberId,
            result.nickname(),
            result.memberGrade().name(),
            result.statusMessage(),
            result.profileImageUrl()
        );
    }

    @Override
    public MemberPersonalInfoResponse getPersonalInfo(Long memberId) {
        MemberPersonalInfoResult result = memberQueryPort.findPersonalInfoById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
        return MemberPersonalInfoResponse.of(
            result.username(),
            result.fullName(),
            result.phoneNumber(),
            result.birthDate(),
            result.gender(),
            result.pushNotificationEnabled(),
            result.marketingInfoEnabled(),
            result.eventInfoEnabled()
        );
    }

    private MemberWithProfileImageResult findProfile(Long memberId) {
        return memberQueryPort.findMemberWithProfileImageById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
