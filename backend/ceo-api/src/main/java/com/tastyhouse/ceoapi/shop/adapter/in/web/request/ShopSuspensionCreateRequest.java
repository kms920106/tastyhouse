package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import java.time.LocalDateTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapplication.shop.port.in.ShopSuspensionCreateCommand;

@Schema(description = "가게 영업 임시중지 등록 요청")
public record ShopSuspensionCreateRequest(
    @NotBlank(message = "임시중지 사유는 필수입니다.")
    @Schema(description = "임시중지 사유", example = "BAD_WEATHER",
        allowableValues = {"EARLY_CLOSE", "OPEN_DELAY", "SHOP_CIRCUMSTANCE", "UNREACHABLE", "TERMINATION_REQUEST", "BAD_WEATHER"},
        requiredMode = Schema.RequiredMode.REQUIRED)
    String reason,

    @Schema(description = "대상 주문수단 목록 (비어있으면 전체 주문수단 대상으로 1건 생성)", example = "[\"DELIVERY\", \"TAKEOUT\"]")
    List<String> orderMethods,

    @NotNull(message = "시작 시각은 필수입니다.")
    @Schema(description = "중지 시작 시각", example = "2026-07-25T09:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime startAt,

    @NotNull(message = "종료 시각은 필수입니다.")
    @Schema(description = "중지 종료 시각", example = "2026-07-25T18:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    LocalDateTime endAt
) {

    public ShopSuspensionCreateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopSuspensionCreateCommand(ceoId, shopId, reason(), orderMethods(), startAt(), endAt());
    }
}
