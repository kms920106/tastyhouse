package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopNoticeResult;

/**
 * 가게 상세에 노출되는 점주 공지 응답.
 *
 * <p>ceo 응답과 달리 {@code exposed}·{@code hidden}·{@code updatedAt}을 담지 않는다 — 사용자 화면이 쓰지
 * 않는 내부 상태이므로 과잉 노출을 피한다("실제 쓰는 필드만" 원칙).
 */
@Schema(description = "가게 점주 공지 응답")
public record ShopNoticeResponse(
    @Schema(description = "공지 ID", example = "12")
    Long id,

    @Schema(description = "공지 본문", example = "이번 주 신메뉴 출시했습니다. 많은 이용 부탁드립니다.")
    String content,

    @Schema(description = "첨부 이미지 URL 목록 (등록 순, 없으면 빈 배열)")
    List<String> imageUrls,

    @Schema(description = "생성 일시", example = "2026-08-15T10:00:00")
    LocalDateTime createdAt
) {
    public static ShopNoticeResponse from(ShopNoticeResult result) {
        return new ShopNoticeResponse(
            result.id(),
            result.content(),
            result.imageUrls(),
            result.createdAt()
        );
    }
}
