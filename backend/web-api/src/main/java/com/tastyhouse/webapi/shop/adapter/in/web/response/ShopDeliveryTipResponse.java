package com.tastyhouse.webapi.shop.adapter.in.web.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.webapplication.shop.port.out.ShopDeliveryTipViewResult;

/**
 * 배달팁 팝업·재견적 응답.
 *
 * <p><b>확정 모드와 범위 모드 두 상태를 한 스키마로 표현한다.</b> 배달 주소와 주문금액이 모두 주어지면
 * {@code deliveryTip}에 확정 금액이, {@code breakdown}에 그 근거가 담긴다(확정 모드). 하나라도 없으면
 * {@code deliveryTip}이 {@code null}, {@code breakdown}이 빈 배열이고 프론트는
 * {@code minDeliveryTip}~{@code maxDeliveryTip} 범위를 보여준다(범위 모드). 두 모드를 엔드포인트로
 * 쪼개지 않은 것은, 팝업이 주소를 고르는 순간 같은 화면에서 범위 → 확정으로 넘어가기 때문이다.
 *
 * <p>{@code minDeliveryTip}/{@code maxDeliveryTip}은 목록·카드·상세가 쓰는 값과 <b>같은 산출 규칙</b>
 * (현재 시각·거리에 의존하지 않는 설정값 전체의 하한/상한)이므로, 같은 가게를 목록에서 보다가 팝업을
 * 열어도 범위 표기가 달라지지 않는다.
 */
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
    /**
     * 금액과 그 근거를 그대로 옮긴다 — 확정/범위 모드 분기, 항목별 근거 문구, 거리별 노출 판정은 모두
     * {@code ShopDeliveryTipViewResult}를 만든 서비스가 끝냈다. 여기서는 어떤 금액도 다시 계산하지 않는다.
     */
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
