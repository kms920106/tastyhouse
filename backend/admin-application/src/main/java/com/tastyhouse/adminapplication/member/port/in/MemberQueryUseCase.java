package com.tastyhouse.adminapplication.member.port.in;

import com.tastyhouse.adminapplication.member.response.MemberDetailResponse;
import com.tastyhouse.adminapplication.member.response.MemberListItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 회원 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code MemberQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface MemberQueryUseCase {

    PaginationResponse<MemberListItemResponse> getMembers(
        String nickname,
        String username,
        String phone,
        String status,
        String grade,
        int page,
        int size
    );

    MemberDetailResponse getMember(Long id);
}
