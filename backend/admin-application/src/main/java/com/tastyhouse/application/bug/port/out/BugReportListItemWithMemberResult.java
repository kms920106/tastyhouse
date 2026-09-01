package com.tastyhouse.application.bug.port.out;

import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;

/**
 * 버그 제보 목록 항목 + 제보 회원 요약 조회 결과.
 *
 * <p><b>챕터 06</b>에서 신설했다. 제보와 제보자는 컨텍스트가 달라 한 포트의 한 쿼리로 채울 수 없고
 * ({@link BugReportQueryPort} + {@code MemberManagementQueryPort}), 두 결과의 합성은 유스케이스
 * ({@code BugReportQueryService})의 책임이다. 승격 이전에는 그 합성 결과가 곧바로 Response였으나,
 * 이제 유스케이스는 프레임워크-프리 결과만 내보내야 하므로 합성 산출물도 결과 타입으로 둔다.
 *
 * <p>{@code member}는 제보자 회원이 조회되지 않으면 {@code null}이다(승격 이전 동작 보존 —
 * 목록에서는 회원 맵에 없는 경우, 상세에서는 조회 실패 시 {@code null}을 그대로 응답에 실었다).
 */
public record BugReportListItemWithMemberResult(
    BugReportListItemResult bugReport,
    MemberWithProfileImageResult member
) {
}
