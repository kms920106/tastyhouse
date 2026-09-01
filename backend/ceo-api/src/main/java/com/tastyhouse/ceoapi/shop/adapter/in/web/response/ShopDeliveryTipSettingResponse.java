package com.tastyhouse.ceoapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipSettingResult;
import com.tastyhouse.application.shop.port.out.ShopDeliveryTipViewResult;

/**
 * 점주 배달팁 설정 화면 통합 응답.
 *
 * <p>배달팁을 한 번도 설정하지 않은 가게도 이 응답을 받는다 — {@code extraTipType}은 {@code "NONE"},
 * {@code distance}는 {@code null}, 목록 3종은 빈 배열, {@code holidayTipAmount}는 0이다.
 */
@Schema(description = "가게 배달팁 설정 통합 응답")
public record ShopDeliveryTipSettingResponse(
    @Schema(description = "구간별 기본 배달팁 목록(주문금액 오름차순). 미설정이면 빈 배열")
    List<ShopDeliveryTipTierItemResponse> tiers,

    @Schema(description = "추가 배달팁 방식", example = "DISTANCE", allowableValues = {"NONE", "DISTANCE", "REGION"})
    String extraTipType,

    @Schema(description = "거리별 추가 배달팁 설정. 거리별을 쓰지 않으면 null")
    ShopDeliveryTipDistanceResponse distance,

    @Schema(description = "지역별 추가 배달팁 목록. 지역별을 쓰지 않으면 빈 배열")
    List<ShopDeliveryTipRegionItemResponse> regions,

    @Schema(description = "시간별 추가 배달팁 목록. 미설정이면 빈 배열")
    List<ShopDeliveryTipScheduleItemResponse> schedules,

    @Schema(description = "공휴일 추가 배달팁(원). 0이면 미설정", example = "2000")
    int holidayTipAmount
) {
    /** 배달팁을 한 번도 설정하지 않은 가게의 추가팁 유형 기본값. */
    private static final String EXTRA_TIP_TYPE_NONE = "NONE";

    public static ShopDeliveryTipSettingResponse from(ShopDeliveryTipViewResult result) {
        ShopDeliveryTipSettingResult setting = result.setting();
        return new ShopDeliveryTipSettingResponse(
            result.tiers().stream()
                .map(ShopDeliveryTipTierItemResponse::from)
                .toList(),
            setting == null ? EXTRA_TIP_TYPE_NONE : setting.extraTipType(),
            ShopDeliveryTipDistanceResponse.from(setting),
            result.regions().stream()
                .map(ShopDeliveryTipRegionItemResponse::from)
                .toList(),
            result.schedules().stream()
                .map(ShopDeliveryTipScheduleItemResponse::from)
                .toList(),
            result.holidayTipAmount()
        );
    }
}
