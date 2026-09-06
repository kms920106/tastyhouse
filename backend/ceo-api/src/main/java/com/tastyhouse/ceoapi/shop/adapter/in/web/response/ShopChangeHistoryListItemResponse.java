package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopChangeHistoryResult;

@Schema(description = "가게 변경이력 목록 항목")
public record ShopChangeHistoryListItemResponse(

    @Schema(description = "이력 ID", example = "1024")
    Long id,

    @Schema(description = "변경 대분류 코드", example = "DELIVERY")
    String category,

    @Schema(description = "변경 대분류 한글 라벨", example = "배달 정보")
    String categoryName,

    @Schema(description = "변경 중분류 코드", example = "DELIVERY_TIP_SCHEDULE")
    String changeType,

    @Schema(description = "변경 중분류 한글 라벨", example = "시간 할증 배달팁")
    String changeTypeName,

    @Schema(description = "조치 유형 코드", example = "UPDATE", allowableValues = {"CREATE", "UPDATE", "DELETE"})
    String actionType,

    @Schema(description = "조치 유형 한글 라벨", example = "수정")
    String actionTypeName,

    @Schema(description = "변경 전 요약. 등록 시 null", example = "18:00~20:00: +1,000원")
    String previousValue,

    @Schema(description = "변경 후 요약. 삭제 시 null", example = "18:00~20:00: +1,500원")
    String newValue,

    @Schema(description = "변경 일시", example = "2026-08-11T19:46:03")
    LocalDateTime changedAt
) {
    public static ShopChangeHistoryListItemResponse from(ShopChangeHistoryResult result) {
        return new ShopChangeHistoryListItemResponse(
            result.id(),
            result.category().name(),
            result.category().getDescription(),
            result.changeType().name(),
            result.changeType().getDescription(),
            result.actionType().name(),
            result.actionType().getDescription(),
            result.previousValue(),
            result.newValue(),
            result.changedAt()
        );
    }
}
