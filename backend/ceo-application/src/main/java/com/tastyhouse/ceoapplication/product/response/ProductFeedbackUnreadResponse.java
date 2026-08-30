package com.tastyhouse.ceoapplication.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 고객 의견 미확인 여부 — 화면 아이콘의 빨간 점 표시용.
 */
@Schema(description = "고객 의견 미확인 여부")
public record ProductFeedbackUnreadResponse(

    @Schema(description = "확인하지 않은 의견이 있는지", example = "true")
    boolean hasUnread
) {

    public static ProductFeedbackUnreadResponse from(boolean hasUnread) {
        return new ProductFeedbackUnreadResponse(hasUnread);
    }
}
