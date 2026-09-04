package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.application.shop.port.in.ShopCeoAssignCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

/**
 * 가게 담당 점주 배정 요청.
 *
 * <p>배정은 개인정보처리시스템 접근권한 부여이므로 대상 점주가 반드시 지정돼야 한다. 해제는 대상이
 * "현재 배정된 점주"로 이미 정해져 있어 본문이 없다(별도 {@code DELETE} 엔드포인트).
 */
@Schema(description = "가게 담당 점주 배정 요청")
public record ShopCeoAssignRequest(

    @NotNull(message = "점주 ID는 필수입니다.")
    @Schema(description = "배정할 점주 ID", example = "7", requiredMode = Schema.RequiredMode.REQUIRED)
    Long ceoId
) {

    public ShopCeoAssignCommand toCommand(Long adminId, Long shopId) {
        return new ShopCeoAssignCommand(adminId, shopId, ceoId);
    }
}
