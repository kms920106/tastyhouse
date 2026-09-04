package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapplication.shop.port.in.ShopClosedDayManagementCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "가게 정기 휴무일 등록 요청")
public record ShopClosedDaySaveRequest(
    @NotBlank(message = "정기 휴무 유형은 필수입니다.")
    @Schema(description = "정기 휴무 유형", example = "EVERY_WEEK_MONDAY", requiredMode = Schema.RequiredMode.REQUIRED)
    String closedDayType
) {

    public ShopClosedDayManagementCreateCommand toCommand(Long adminId, Long shopId) {
        return new ShopClosedDayManagementCreateCommand(adminId, shopId, closedDayType);
    }
}
