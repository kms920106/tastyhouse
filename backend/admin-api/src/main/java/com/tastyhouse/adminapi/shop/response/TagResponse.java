package com.tastyhouse.adminapi.shop.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "태그 응답")
public record TagResponse(
    @Schema(description = "태그 ID", example = "1")
    Long id,

    @Schema(description = "태그명", example = "혼밥")
    String tagName
) {
    public static TagResponse from(Long id, String tagName) {
        return new TagResponse(id, tagName);
    }
}
