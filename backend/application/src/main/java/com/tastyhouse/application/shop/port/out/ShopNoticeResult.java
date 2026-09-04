package com.tastyhouse.application.shop.port.out;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 점주 공지 조회 결과.
 *
 * <p>{@code imageUrls}는 DAO가 {@code FileUrlResolver}로 표시용 URL까지 완성해 담는다 — 소비 Service는
 * 파일에 대해 아무것도 알지 않는다("응답 record 파일/이미지 필드 URL 규칙").
 */
public record ShopNoticeResult(
    Long id,
    Long shopId,
    String content,
    List<String> imageUrls,
    boolean exposed,
    boolean hidden,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
}
