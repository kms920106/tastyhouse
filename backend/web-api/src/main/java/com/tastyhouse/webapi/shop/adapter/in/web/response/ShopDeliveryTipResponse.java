package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopDeliveryTipViewResult;

@Schema(description = "가게 배달팁 조회 응답")
public record ShopDeliveryTipResponse(
    @Schema(description = "확정 배달팁(원). 배달 주소와 주문금액이 모두 주어졌을 때만 값이 있고, 확정할 수 없으면 null입니다.", example = "3000")
    Integer deliveryTip,

    @Schema(description = "배달팁 최소 금액(원). 구간별·추가 배달팁을 합산한 하한. 0이면 배달팁 없음", example = "2000")
    int minDeliveryTip,

    @Schema(description = "배달팁 최대 금액(원). 고객 주소가 확정되기 전 상한", example = "4000")
    int maxDeliveryTip,

    @Schema(description = "확정 배달팁의 항목별 근거. 확정할 수 없거나 금액이 0인 항목은 포함되지 않습니다.")
    List<ShopDeliveryTipBreakdownItem> breakdown,

    @Schema(description = "주문금액 구간별 배달팁 표. 주문금액 오름차순입니다.")
    List<ShopDeliveryTipTierItem> tiers,

    @Schema(description = "추가 배달팁 방식(NONE: 없음, DISTANCE: 거리별, REGION: 지역별)", example = "DISTANCE")
    String extraTipType,

    @Schema(description = "거리별 추가 배달팁 설정. 거리별을 쓰지 않는 가게는 null입니다.")
    ShopDeliveryTipDistanceItem distance,

    @Schema(description = "지역별 추가 배달팁 목록. 지역별을 쓰지 않는 가게는 빈 배열입니다.")
    List<ShopDeliveryTipRegionItem> regions,

    @Schema(description = "시간대별 추가 배달팁 목록")
    List<ShopDeliveryTipScheduleItem> schedules,

    @Schema(description = "공휴일 추가 배달팁(원). 0이면 미설정", example = "1000")
    int holidayTipAmount
) {
    public static ShopDeliveryTipResponse from(ShopDeliveryTipViewResult result) {
        return new ShopDeliveryTipResponse(
            result.deliveryTip(),
            result.minDeliveryTip(),
            result.maxDeliveryTip(),
            result.breakdown().stream().map(ShopDeliveryTipBreakdownItem::from).toList(),
            result.tiers().stream().map(ShopDeliveryTipTierItem::from).toList(),
            result.extraTipType(),
            result.distance() == null ? null : ShopDeliveryTipDistanceItem.from(result.distance()),
            result.regions().stream().map(ShopDeliveryTipRegionItem::from).toList(),
            result.schedules().stream().map(ShopDeliveryTipScheduleItem::from).toList(),
            result.holidayTipAmount()
        );
    }
}
