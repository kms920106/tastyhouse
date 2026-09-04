package com.tastyhouse.ceoapi.shop.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.application.shop.port.in.ShopHolidayClosureUpdateCommand;

@Schema(description = "가게 공휴일 휴무 설정 변경 요청")
public record ShopHolidayClosureUpdateRequest(
    @NotNull(message = "공휴일 휴무 여부는 필수입니다.")
    @Schema(description = "공휴일 휴무 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    Boolean closedOnPublicHolidays
) {

    public ShopHolidayClosureUpdateCommand toCommand(Long ceoId, Long shopId) {
        return new ShopHolidayClosureUpdateCommand(ceoId, shopId, closedOnPublicHolidays());
    }
}
