package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopNoticeResult;

@Schema(description = "점주 공지 응답")
public record ShopNoticeResponse(
    @Schema(description = "공지 ID", example = "12")
    Long id,

    @Schema(description = "공지 본문", example = "이번 주 신메뉴 출시했습니다. 많은 이용 부탁드립니다.")
    String content,

    @Schema(description = "첨부 이미지 URL 목록 (등록 순, 없으면 빈 배열)")
    List<String> imageUrls,

    @Schema(description = "앱 노출 여부", example = "true")
    boolean exposed,

    @Schema(description = "관리자 게시중단 여부", example = "false")
    boolean hidden,

    @Schema(description = "생성 일시", example = "2026-08-15T10:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정 일시", example = "2026-08-15T10:00:00")
    LocalDateTime updatedAt
) {
    public static ShopNoticeResponse from(ShopNoticeResult result) {
        return new ShopNoticeResponse(
            result.id(),
            result.content(),
            result.imageUrls(),
            result.exposed(),
            result.hidden(),
            result.createdAt(),
            result.updatedAt()
        );
    }
}
