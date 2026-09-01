package com.tastyhouse.webapplication.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.member.port.out.MemberPersonalInfoResult;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;
import com.tastyhouse.webapplication.member.port.in.MemberQueryUseCase;

/**
 * 회원 조회 서비스.
 *
 * <p>프로필 카드·개인정보 조회와 닉네임·휴대폰 사용 가능 여부까지 모두 읽기 포트
 * ({@link MemberQueryPort})의 투영으로 답한다 — 전부 표현 목적 조회라 write 포트를 주입하지 않는다
 * (CQRS 교차 주입 금지).
 *
 * <p>프로필 이미지는 DAO가 표시용 URL까지 변환해 담으므로, 이 서비스는 그 값을 그대로 읽기 계약에 실어 보낸다 —
 * Response 조립은 web-api의 응답 record가 담당한다(챕터 10).
 */
@Service
@Transactional(readOnly = true)
public class MemberQueryService implements MemberQueryUseCase {

    private final MemberQueryPort memberQueryPort;

    public MemberQueryService(MemberQueryPort memberQueryPort) {
        this.memberQueryPort = memberQueryPort;
    }

    @Override
    public boolean checkNicknameAvailability(String nickname) {
        return !memberQueryPort.existsByNickname(nickname);
    }

    @Override
    public boolean checkPhoneAvailability(String phoneNumber) {
        return !memberQueryPort.existsByActivePhoneNumber(phoneNumber);
    }

    /**
     * 타 회원 프로필 조회.
     *
     * <p>본인 조회({@link #getMyProfile})와 같은 계약을 반환하고, 식별자를 응답에 실을지는 web-api의
     * 응답 record가 가른다({@code MemberProfileResponse}는 {@code id}가 없고 {@code MyProfileResponse}는 있다).
     */
    @Override
    public MemberWithProfileImageResult getMemberProfile(Long targetMemberId) {
        return findProfile(targetMemberId);
    }

    /** 본인 프로필 조회 — 소유권 비교용 식별자를 계약에 담아 내보낸다. */
    @Override
    public MemberWithProfileImageResult getMyProfile(Long memberId) {
        return findProfile(memberId);
    }

    @Override
    public MemberPersonalInfoResult getPersonalInfo(Long memberId) {
        return memberQueryPort.findPersonalInfoById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private MemberWithProfileImageResult findProfile(Long memberId) {
        return memberQueryPort.findMemberWithProfileImageById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
