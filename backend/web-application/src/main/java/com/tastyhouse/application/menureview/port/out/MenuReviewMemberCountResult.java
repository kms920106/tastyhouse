package com.tastyhouse.application.menureview.port.out;

import java.time.LocalDateTime;

/**
 * 기간 내 회원별 메뉴 평가 수 집계 — 랭킹·회원등급 합산용.
 *
 * <p>{@code lastMenuReviewAt}은 동점자 정렬(마지막 작성 이른 순)의 근거다. 소비 측
 * ({@code MemberReviewCountQueryDao})이 REVIEW 집계와 병합할 때 <b>더 늦은 쪽</b>을 취한다.
 */
public record MenuReviewMemberCountResult(
    Long memberId,
    Long menuReviewCount,
    LocalDateTime lastMenuReviewAt
) {
}
