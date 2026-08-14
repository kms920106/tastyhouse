package com.tastyhouse.ceoapi.review.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시중단 요청 사유 카탈로그 항목")
public record ReviewBlindReasonCatalogResponse(
    @Schema(description = "사유 코드", example = "PROFANITY")
    String code,

    @Schema(description = "사유 한글명", example = "욕설·비방")
    String description
) {

    public static ReviewBlindReasonCatalogResponse from(String code, String description) {
        return new ReviewBlindReasonCatalogResponse(
            code,
            description
        );
    }
}
