package com.tastyhouse.domain.member.port;

import com.tastyhouse.domain.member.vo.MemberId;

/**
 * 등급 산정 입력값 — 집계 기간 동안 한 회원이 작성한 리뷰 수.
 *
 * <p>{@link MemberReviewCountPort}의 반환 타입으로, member 컨텍스트가 소유하는 순수 값 객체다. 조회 구현
 * (QueryDSL 투영)은 infrastructure-module이 담당하며, 그 결과를 이 타입으로 옮겨 담아 도메인에 넘긴다.
 *
 * <p>등급 판정에 필요한 것은 리뷰 수뿐이므로 rank의 동명 값 객체와 달리 {@code lastReviewAt}(동수 시
 * 순위 결정용 tie-breaker)을 담지 않는다 — 소비자가 실제로 쓰는 필드만 갖는다.
 */
public record MemberReviewCount(
    MemberId memberId,
    Long reviewCount
) {

    public static MemberReviewCount of(
        MemberId memberId,
        Long reviewCount
    ) {
        return new MemberReviewCount(
            memberId,
            reviewCount
        );
    }
}
