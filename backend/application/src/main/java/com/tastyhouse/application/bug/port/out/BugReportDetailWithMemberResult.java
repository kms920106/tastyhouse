package com.tastyhouse.application.bug.port.out;

import com.tastyhouse.application.member.port.out.MemberWithProfileImageResult;

/**
 * 버그 제보 상세 + 제보 회원 요약 조회 결과.
 *
 * <p><b>챕터 06</b>에서 신설했다. 신설 근거는 목록용 형제
 * {@link BugReportListItemWithMemberResult}와 같다 — 제보와 제보자는 서로 다른 읽기 포트에서 오고,
 * 그 합성은 유스케이스의 책임이나 산출물은 프레임워크-프리여야 한다.
 *
 * <p>{@code member}는 제보자 회원이 조회되지 않으면 {@code null}이다(승격 이전 동작 보존).
 */
public record BugReportDetailWithMemberResult(
    BugReportDetailResult bugReport,
    MemberWithProfileImageResult member
) {
}
