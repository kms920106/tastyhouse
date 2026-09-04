package com.tastyhouse.application.review.port.out;

import java.time.LocalDate;

/**
 * 점주 답변 가능 기간 — 답변 마감일과 지금 답변할 수 있는지 여부.
 *
 * <p><b>챕터 09</b>에서 신설. 마감일은 리뷰 작성일 + {@code ReviewOwnerReply#REPLY_PERIOD_DAYS}인데
 * 그 상수는 <b>도메인 모델</b>이 소유하므로 api 모듈이 참조할 수 없다
 * ({@code apiModuleShouldBeDomainModelFree}). 계산을 application에 남기고 결과만 나른다.
 *
 * <p>{@code replyable} 판정에 "오늘"이 필요한 것도 이 계산을 표현 계층에 두지 않는 이유다 — 표현
 * 계약이 시계를 읽으면 응답 조립이 시점에 따라 달라지는 순수하지 않은 함수가 된다.
 */
public record ShopReviewReplyWindow(
    LocalDate replyDeadline,
    boolean replyable
) {
}
