package com.tastyhouse.ceoapplication.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 배달가능지역 일괄 처리 결과.
 *
 * <p>{@code skippedCount}를 따로 내려주는 이유는, 일괄 추가가 <b>이미 등록된 동을 실패가 아니라 건너뛰기</b>로
 * 처리하기 때문이다. 요청 개수와 실제 반영 개수가 다를 수 있으므로 화면이 "20개 중 5개는 이미 등록돼
 * 있었습니다"를 정확히 설명할 수 있어야 한다.
 */
@Schema(description = "배달가능지역 일괄 처리 결과")
public record ShopDeliveryAreaBulkResponse(
    @Schema(description = "요청한 개수(중복 제거 후)", example = "20")
    int requestedCount,

    @Schema(description = "실제로 새로 등록된 개수", example = "15")
    int addedCount,

    @Schema(description = "이미 등록돼 있어 건너뛴 개수", example = "5")
    int skippedCount,

    @Schema(description = "반영 후 이 가게의 총 배달가능지역 개수", example = "42")
    int totalCount
) {

    public static ShopDeliveryAreaBulkResponse from(
        int requestedCount,
        int addedCount,
        int skippedCount,
        int totalCount
    ) {
        return new ShopDeliveryAreaBulkResponse(
            requestedCount,
            addedCount,
            skippedCount,
            totalCount
        );
    }
}
