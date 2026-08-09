package com.tastyhouse.ceoapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 배달가능지역 일괄 삭제 결과.
 *
 * <p>지역별 배달팁이 참조하는 동이 하나라도 섞이면 <b>한 건도 지우지 않고 409</b>로 끝나므로, 이 응답은
 * 항상 "전부 지워진" 상태만 나타낸다(부분 삭제 결과가 아니다).
 */
@Schema(description = "배달가능지역 일괄 삭제 결과")
public record ShopDeliveryAreaBulkDeleteResponse(
    @Schema(description = "삭제된 개수", example = "12")
    int removedCount,

    @Schema(description = "반영 후 이 가게의 총 배달가능지역 개수", example = "30")
    int totalCount
) {

    public static ShopDeliveryAreaBulkDeleteResponse from(
        int removedCount,
        int totalCount
    ) {
        return new ShopDeliveryAreaBulkDeleteResponse(
            removedCount,
            totalCount
        );
    }
}
