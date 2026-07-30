package com.tastyhouse.core.domain.rank.domain.port;

import java.time.LocalDateTime;

import com.tastyhouse.core.domain.member.domain.vo.MemberId;

/**
 * 랭킹 집계 입력값 — 특정 기간 동안 한 회원이 작성한 리뷰 수와 마지막 작성 시각.
 *
 * <p>{@link MemberReviewCountPort}의 반환 타입으로, 도메인 계층이 소유하는 순수 값 객체다. 조회 구현
 * (QueryDSL 투영)은 infrastructure-module이 담당하며, 그 결과를 이 타입으로 옮겨 담아 도메인에 넘긴다.
 * 도메인 서비스는 이 값만 보고 순위를 매기므로 리뷰 도메인의 read model에 직접 결합되지 않는다.
 */
public record MemberReviewCount(
    MemberId memberId,
    Long reviewCount,
    LocalDateTime lastReviewAt
) {

    public static MemberReviewCount of(
        MemberId memberId,
        Long reviewCount,
        LocalDateTime lastReviewAt
    ) {
        return new MemberReviewCount(
            memberId,
            reviewCount,
            lastReviewAt
        );
    }
}
