package com.tastyhouse.adminapi.shop.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "라이더 안내 문구 변경 이력")
public record ShopRiderGuideHistoryResponse(
    @Schema(description = "이력 ID", example = "12")
    Long id,

    @Schema(description = "변경 주체", example = "ADMIN", allowableValues = {"CEO", "ADMIN"})
    String actorType,

    @Schema(description = "변경 주체 ID (CEO.id 또는 ADMIN.id)", example = "3")
    Long actorId,

    @Schema(description = "조치 유형", example = "REVISION_REQUEST",
        allowableValues = {"UPDATE", "REVISION_REQUEST", "DELETION"})
    String actionType,

    @Schema(description = "변경 전 문구", example = "18인치 피자는 자동차 라이더만 수행 부탁드립니다.")
    String previousVisitGuide,

    @Schema(description = "변경 후 문구 (삭제 조치 시 null)", example = "18인치 피자는 자동차 라이더만 수행 부탁드립니다.")
    String newVisitGuide,

    @Schema(description = "관리자 조치 사유 (점주 변경 시 null)",
        example = "배차를 특정하는 문구입니다. 위치 안내로 수정해 주세요.")
    String reason,

    @Schema(description = "이력 생성 일시", example = "2026-08-08T21:10:00")
    LocalDateTime createdAt
) {

    public static ShopRiderGuideHistoryResponse from(
        Long id,
        String actorType,
        Long actorId,
        String actionType,
        String previousVisitGuide,
        String newVisitGuide,
        String reason,
        LocalDateTime createdAt
    ) {
        return new ShopRiderGuideHistoryResponse(
            id,
            actorType,
            actorId,
            actionType,
            previousVisitGuide,
            newVisitGuide,
            reason,
            createdAt
        );
    }
}
