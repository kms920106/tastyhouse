package com.tastyhouse.application.shop.port.out;

/**
 * 점주 공지 첨부 이미지 조회 결과.
 *
 * <p>공지 본문과 별도 쿼리로 읽어 {@code shopNoticeId}로 묶는다 — 1:N 조인으로 한 번에 읽으면 공지 행이
 * 이미지 수만큼 중복되어 재조립이 필요하다. {@code imageUrl}은 투영 직후 표시용 URL로 완성된다.
 */
public record ShopNoticeImageResult(
    Long shopNoticeId,
    String imageUrl,
    int sortOrder
) {
}
