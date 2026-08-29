package com.tastyhouse.application.review.port.out;

import java.time.LocalDateTime;

/**
 * 기간 내 회원별 리뷰 수 집계 결과 — 리뷰 테이블을 회원 단위로 그룹 투영한다.
 *
 * <p>리뷰 도메인의 read model이므로 이 패키지가 소유한다(과거에는 rank의 application dto에 있었으나,
 * 실제 소유 도메인이 review이고 소비자가 rank 집계·회원 등급 산정 둘로 갈려 여기로 이관했다).
 *
 * <p>정렬은 조회 측이 보장한다 — 리뷰 수 내림차순, 동수면 마지막 작성이 이른 회원 우선, 그다음 회원 ID
 * 오름차순. 랭킹 집계가 이 순서를 그대로 순위로 사용한다.
 */
public record MemberReviewCountResult(
    Long memberId,
    Long reviewCount,
    LocalDateTime lastReviewAt
) {
}
