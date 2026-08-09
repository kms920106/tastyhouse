package com.tastyhouse.ceoapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 저장하면 닫히지만 <b>닫을 수 없는</b> 행정동 한 건.
 *
 * <p>지역별 배달팁이 그 동을 참조하고 있으면 배달지역에서 뺄 수 없다(배달팁이 배달 불가 지역을 가리키게
 * 되기 때문). 미리보기가 이 목록을 먼저 보여주므로 점주는 저장에서 409를 맞기 전에 배달팁을 정리할 수 있다.
 */
@Schema(description = "닫을 수 없는 행정동 한 건")
public record ShopDeliveryAreaBlockedResponse(
    @Schema(description = "행정동 ID", example = "1101053")
    long adminDongId,

    @Schema(description = "행정동 전체 이름", example = "서울특별시 강남구 역삼1동")
    String regionName,

    @Schema(description = "닫을 수 없는 사유", example = "REGION_TIP", allowableValues = {"REGION_TIP"})
    String reason
) {

    public static ShopDeliveryAreaBlockedResponse from(
        long adminDongId,
        String regionName,
        String reason
    ) {
        return new ShopDeliveryAreaBlockedResponse(
            adminDongId,
            regionName,
            reason
        );
    }
}
