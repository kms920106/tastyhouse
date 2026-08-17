package com.tastyhouse.domain.review.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewBlindRequestId;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 리뷰 게시중단이 <b>승인</b>됐음을 알리는 도메인 이벤트.
 *
 * <p>수신자는 리뷰 작성자({@code reviewerMemberId})다 — 자기 리뷰가 게시중단됐고 30일 뒤 재노출되거나
 * 지금 삭제에 동의할 수 있다는 것을 알려야 하기 때문이다. 승인 절차가 이미 로드한 {@code Review}에서
 * 꺼내므로 추가 조회가 0회다.
 *
 * <p>반려·취소·만료·삭제는 발행하지 않는다 — 원문이 알림을 요구하는 시점은 게시중단 통지 하나뿐이고,
 * 나머지는 고객이 행동해야 할 일이 없다.
 */
public record ReviewBlindApprovedEvent(
    ReviewId reviewId,
    MemberId reviewerMemberId,
    ReviewBlindRequestId blindRequestId,
    LocalDateTime blindUntil,
    LocalDateTime occurredAt
) {

    public static ReviewBlindApprovedEvent of(
        ReviewId reviewId,
        MemberId reviewerMemberId,
        ReviewBlindRequestId blindRequestId,
        LocalDateTime blindUntil,
        LocalDateTime occurredAt
    ) {
        return new ReviewBlindApprovedEvent(
            reviewId,
            reviewerMemberId,
            blindRequestId,
            blindUntil,
            occurredAt
        );
    }
}
