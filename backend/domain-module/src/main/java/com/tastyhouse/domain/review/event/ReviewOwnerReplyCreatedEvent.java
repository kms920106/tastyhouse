package com.tastyhouse.domain.review.event;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.review.vo.ReviewId;
import com.tastyhouse.domain.review.vo.ReviewOwnerReplyId;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 사장님 답변이 <b>신규 등록</b>됐음을 알리는 도메인 이벤트.
 *
 * <p>원문("파트너님이 댓글을 달면, 고객에게 바로 '알림'이 갑니다")대로 <b>등록에서만 발행</b>한다 —
 * 수정·삭제는 발행하지 않는다. 수정마다 알림이 가면 같은 답변으로 반복 알림이 쌓여 스팸이 된다.
 *
 * <p>{@code reviewerMemberId}는 알림 수신자(리뷰 작성자)다. 등록 절차가 이미 로드한 {@code Review}에서
 * 꺼내므로 추가 조회가 0회다.
 */
public record ReviewOwnerReplyCreatedEvent(
    ReviewId reviewId,
    MemberId reviewerMemberId,
    ShopId shopId,
    ReviewOwnerReplyId ownerReplyId,
    LocalDateTime occurredAt
) {
}
