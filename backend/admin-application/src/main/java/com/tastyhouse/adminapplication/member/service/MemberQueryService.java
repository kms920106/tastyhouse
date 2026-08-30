package com.tastyhouse.adminapplication.member.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.member.model.MemberGrade;
import com.tastyhouse.domain.member.model.MemberStatus;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.member.port.out.MemberListItemResult;
import com.tastyhouse.application.member.port.out.MemberManagementDetailResult;
import com.tastyhouse.application.member.port.out.MemberQueryPort;
import com.tastyhouse.application.member.port.out.MemberSearchCondition;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapplication.member.response.MemberDetailResponse;
import com.tastyhouse.adminapplication.member.response.MemberListItemResponse;
import com.tastyhouse.adminapplication.member.port.in.MemberQueryUseCase;

/**
 * 회원 관리 조회 서비스.
 *
 * <p>목록·검색도 상세도 모두 읽기 포트({@link MemberQueryPort})의 투영으로 답한다 — 상세 응답이
 * 아이디·연락처·알림 수신 설정 등 많은 필드를 요구하지만 전부 화면에 그대로 실리는 표현용이라,
 * 애그리거트를 로드할 이유가 없다(write 포트 미주입).
 *
 * <p>HTTP 경계에서 받은 {@code String} 필터값은 여기서 core enum으로 승격하고, Response로 내보낼 때는
 * 다시 {@code name()} 문자열로 되돌린다(api 모듈은 core enum을 노출하지 않는다).
 */
@Service
@Transactional(readOnly = true)
public class MemberQueryService implements MemberQueryUseCase {

    private final MemberQueryPort memberQueryPort;

    public MemberQueryService(MemberQueryPort memberQueryPort) {
        this.memberQueryPort = memberQueryPort;
    }

    @Override
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
        PageResult<MemberListItemResponse> pageResult = memberQueryPort.findMembers(condition, pageQuery)
            .map(this::toMemberListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    @Override
    public MemberDetailResponse getMember(Long id) {
        MemberManagementDetailResult member = memberQueryPort.findManagementDetailById(MemberId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        return toMemberDetailResponse(member);
    }

    private MemberDetailResponse toMemberDetailResponse(MemberManagementDetailResult member) {
        String profileImageUrl = memberQueryPort.findProfileImageUrl(MemberId.of(member.id())).orElse(null);

        return MemberDetailResponse.from(
            member.id(),
            member.username(),
            member.nickname(),
            member.fullName(),
            member.phoneNumber(),
            member.gender(),
            member.birthDate(),
            member.memberGrade(),
            member.memberStatus(),
            member.statusMessage(),
            profileImageUrl,
            member.pushNotificationEnabled(),
            member.marketingInfoEnabled(),
            member.eventInfoEnabled(),
            member.createdAt()
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
