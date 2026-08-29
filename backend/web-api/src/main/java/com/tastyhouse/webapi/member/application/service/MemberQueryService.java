package com.tastyhouse.webapi.member.application.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.Member;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.repository.MemberRepository;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.infrastructure.member.query.MemberQueryDao;
import com.tastyhouse.infrastructure.member.query.MemberWithProfileImageResult;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberNicknameAvailabilityResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberPersonalInfoResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberPhoneAvailabilityResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MemberProfileResponse;
import com.tastyhouse.webapi.member.adapter.in.web.response.MyProfileResponse;

/**
 * 회원 조회 서비스.
 *
 * <p>프로필 카드 조회는 infra read 어댑터({@link MemberQueryDao})로 투영하고, 개인정보 조회는 회원
 * 도메인 모델의 여러 필드를 그대로 노출해야 하므로 write 포트({@link MemberRepository})의 단건 로드를
 * 쓴다. 닉네임·휴대폰 사용 가능 여부는 원시 boolean 반환이라 write 포트의 중복 검증을 그대로 쓴다.
 *
 * <p>프로필 이미지는 DAO가 표시용 URL까지 변환해 담으므로, 이 서비스는 그 값을 그대로 응답에 전달한다.
 */
@Service
@Transactional(readOnly = true)
public class MemberQueryService {

    private final MemberQueryDao memberQueryDao;
    private final MemberRepository memberRepository;

    public MemberQueryService(MemberQueryDao memberQueryDao, MemberRepository memberRepository) {
        this.memberQueryDao = memberQueryDao;
        this.memberRepository = memberRepository;
    }

    public MemberNicknameAvailabilityResponse checkNicknameAvailability(String nickname) {
        boolean available = !memberRepository.existsByNickname(nickname);
        return MemberNicknameAvailabilityResponse.from(available);
    }

    public MemberPhoneAvailabilityResponse checkPhoneAvailability(String phoneNumber) {
        boolean available = !memberRepository.existsByPhoneNumberAndStatusNot(phoneNumber, MemberStatus.DELETED);
        return MemberPhoneAvailabilityResponse.from(available);
    }

    /** 타 회원 프로필 조회 — 식별자는 노출하지 않는다. */
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

    public MemberPersonalInfoResponse getPersonalInfo(Long memberId) {
        Member member = getMember(memberId);
        return MemberPersonalInfoResponse.of(
            member.getUsername(),
            member.getFullName(),
            member.getPhoneNumber().value(),
            member.getBirthDate(),
            member.getGender().name(),
            member.isPushNotificationEnabled(),
            member.isMarketingInfoEnabled(),
            member.isEventInfoEnabled()
        );
    }

    /**
     * 회원 도메인 모델 단건 로드. 인증·비밀번호 검증처럼 도메인 모델 자체가 필요한 web 내부 협력자가
     * 재사용한다.
     */
    public Member getMember(Long memberId) {
        return memberRepository.findById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }

    private MemberWithProfileImageResult findProfile(Long memberId) {
        return memberQueryDao.findMemberWithProfileImageById(MemberId.of(memberId))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
