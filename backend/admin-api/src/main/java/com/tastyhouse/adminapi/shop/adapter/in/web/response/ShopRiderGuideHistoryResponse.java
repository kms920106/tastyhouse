package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopRiderGuideHistoryResult;

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

    public static ShopRiderGuideHistoryResponse from(ShopRiderGuideHistoryResult result) {
        return new ShopRiderGuideHistoryResponse(
            result.id(),
            result.actorType().name(),
            result.actorId(),
            result.actionType().name(),
            result.previousVisitGuide(),
            result.newVisitGuide(),
            result.reason(),
            result.createdAt()
        );
    }
}
