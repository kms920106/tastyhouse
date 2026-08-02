package com.tastyhouse.adminapi.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.domain.model.Member;
import com.tastyhouse.domain.member.domain.model.MemberGrade;
import com.tastyhouse.domain.member.domain.model.MemberStatus;
import com.tastyhouse.domain.member.domain.repository.MemberRepository;
import com.tastyhouse.domain.member.domain.vo.MemberId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.member.query.MemberListItemResult;
import com.tastyhouse.infrastructure.member.query.MemberQueryDao;
import com.tastyhouse.infrastructure.member.query.MemberSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.member.response.MemberDetailResponse;
import com.tastyhouse.adminapi.member.response.MemberListItemResponse;

/**
 * 회원 관리 조회 서비스.
 *
 * <p>목록·검색은 infra read 어댑터({@link MemberQueryDao})로 투영하고, 상세는 회원 도메인 모델을 그대로
 * 노출해야 해서 write 포트({@link MemberRepository})의 단건 로드를 쓴다 — 상세 응답이 아이디·연락처·
 * 알림 수신 설정 등 도메인 모델의 거의 모든 필드를 필요로 하므로 별도 read model을 두는 이점이 없다.
 *
 * <p>HTTP 경계에서 받은 {@code String} 필터값은 여기서 core enum으로 승격하고, Response로 내보낼 때는
 * 다시 {@code name()} 문자열로 되돌린다(api 모듈은 core enum을 노출하지 않는다).
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberQueryService {

    private final MemberQueryDao memberQueryDao;
    private final MemberRepository memberRepository;

    public PaginationResponse<MemberListItemResponse> getMembers(
        String nickname,
        String username,
        String phone,
        String status,
        String grade,
        int page,
        int size
    ) {
        MemberSearchCondition condition = MemberSearchCondition.of(
            nickname,
            username,
            phone,
            status == null ? null : MemberStatus.from(status),
            grade == null ? null : MemberGrade.from(grade)
        );
        PageQuery pageQuery = PageQuery.of(page, size);
        PageResult<MemberListItemResponse> pageResult = memberQueryDao.findMembers(condition, pageQuery)
            .map(this::toMemberListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public MemberDetailResponse getMember(Long id) {
        MemberId memberId = MemberId.of(id);
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return toMemberDetailResponse(member);
    }

    private MemberDetailResponse toMemberDetailResponse(Member member) {
        String profileImageUrl = memberQueryDao.findProfileImageUrl(member.getMemberId()).orElse(null);

        return MemberDetailResponse.from(
            member.getId(),
            member.getUsername(),
            member.getNickname(),
            member.getFullName(),
            member.getPhoneNumber() != null ? member.getPhoneNumber().value() : null,
            member.getGender().name(),
            member.getBirthDate(),
            member.getMemberGrade().name(),
            member.getMemberStatus().name(),
            member.getStatusMessage(),
            profileImageUrl,
            member.isPushNotificationEnabled(),
            member.isMarketingInfoEnabled(),
            member.isEventInfoEnabled(),
            member.getCreatedAt()
        );
    }

    private MemberListItemResponse toMemberListItemResponse(MemberListItemResult result) {
        return MemberListItemResponse.from(
            result.id(),
            result.username(),
            result.nickname(),
            result.fullName(),
            result.phoneNumber(),
            result.gender().name(),
            result.memberGrade().name(),
            result.memberStatus().name(),
            result.profileImageUrl(),
            result.createdAt()
        );
    }

}
