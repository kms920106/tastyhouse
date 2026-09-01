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
import com.tastyhouse.application.member.port.out.MemberManagementDetailWithProfileImageResult;
import com.tastyhouse.application.member.port.out.MemberManagementQueryPort;
import com.tastyhouse.application.member.port.out.MemberSearchCondition;
import com.tastyhouse.adminapplication.member.port.in.MemberQueryUseCase;

/**
 * 회원 관리 조회 서비스.
 *
 * <p>목록·검색도 상세도 모두 읽기 포트({@link MemberManagementQueryPort})의 투영으로 답한다 — 상세 응답이
 * 아이디·연락처·알림 수신 설정 등 많은 필드를 요구하지만 전부 화면에 그대로 실리는 표현용이라,
 * 애그리거트를 로드할 이유가 없다(write 포트 미주입).
 *
 * <p>HTTP 경계에서 받은 {@code String} 필터값은 여기서 core enum으로 승격한다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 상세만은 프로필 이미지 URL이 별도 조회라 같은 읽기 트랜잭션 안에서
 * {@link MemberManagementDetailWithProfileImageResult}로 묶어 반환한다.
 */
@Service
@Transactional(readOnly = true)
public class MemberQueryService implements MemberQueryUseCase {

    private final MemberManagementQueryPort memberManagementQueryPort;

    public MemberQueryService(MemberManagementQueryPort memberManagementQueryPort) {
        this.memberManagementQueryPort = memberManagementQueryPort;
    }

    @Override
    public PageResult<MemberListItemResult> getMembers(
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
        return memberManagementQueryPort.findMembers(condition, pageQuery);
    }

    @Override
    public MemberManagementDetailWithProfileImageResult getMember(Long id) {
        MemberManagementDetailResult member = memberManagementQueryPort.findManagementDetailById(MemberId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.MEMBER_NOT_FOUND));

        String profileImageUrl = memberManagementQueryPort.findProfileImageUrl(MemberId.of(member.id())).orElse(null);

        return new MemberManagementDetailWithProfileImageResult(member, profileImageUrl);
    }

}
