package com.tastyhouse.infrastructure.shop.query;

import java.time.LocalDateTime;

/**
 * 점주 공지 본문 투영 중간 결과(이미지 결합 전).
 *
 * <p>{@code imageUrls}는 별도 쿼리로 읽어 붙이므로, 본문만 담는 이 record를 먼저 투영한 뒤
 * {@link ShopNoticeResult}로 재조립한다. {@code Projections.constructor}가 리플렉션으로 생성자를
 * 찾으므로 반드시 {@code public}이어야 한다({@code QueryResultRecordVisibilityTest}가 가드).
 */
public record ShopNoticeRow(
    Long id,
    Long shopId,
    String content,
    boolean exposed,
    boolean hidden,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
