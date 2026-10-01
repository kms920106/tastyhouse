package com.tastyhouse.ceoapi.review.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ReviewBlindReasonView;

@Schema(description = "게시중단 요청 사유 카탈로그 항목")
public record ReviewBlindReasonCatalogResponse(
    @Schema(description = "사유 코드", example = "PROFANITY")
    String code,

    @Schema(description = "사유 한글명", example = "욕설·비방")
    String description
) {

    public static ReviewBlindReasonCatalogResponse from(ReviewBlindReasonView view) {
        return new ReviewBlindReasonCatalogResponse(
            view.code(),
            view.description()
        );
    }
}
