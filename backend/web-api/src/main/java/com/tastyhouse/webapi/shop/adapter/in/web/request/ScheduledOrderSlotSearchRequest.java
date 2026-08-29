package com.tastyhouse.webapi.shop.adapter.in.web.request;

import java.util.Locale;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * 예약 가능 수령시간 슬롯 조회 요청.
 *
 * <p>주문 방법에 따라 리드타임(배달 2시간 / 포장 1시간)과 슬롯 표기(범위 / 단일 시각)가 달라지므로
 * 필수다. 값이 하나여도 record로 감싼다(공통 지침의 조회 파라미터 수신 규칙).
 */
@Schema(description = "예약 가능 수령시간 슬롯 조회 요청")
public record ScheduledOrderSlotSearchRequest(
    @NotBlank(message = "주문 방법은 필수입니다.")
    @Schema(
        description = "주문 방법. 예약주문은 DELIVERY·TAKEOUT만 지원하며, 그 외에는 available=false로 응답합니다.",
        example = "DELIVERY",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"},
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    String orderMethod
) {

    /**
     * 대소문자·공백을 정규화한다 — 소비 Service가 방어 분기를 두지 않도록 HTTP 경계에서 끝낸다.
     * 문자열→enum 승격은 여기서 하지 않는다(Request record는 domain-free).
     */
    public ScheduledOrderSlotSearchRequest {
        orderMethod = orderMethod == null ? null : orderMethod.strip().toUpperCase(Locale.ROOT);
    }
}
