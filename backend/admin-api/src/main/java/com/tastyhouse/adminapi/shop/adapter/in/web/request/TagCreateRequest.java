package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapplication.shop.port.in.TagCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "태그 등록 요청")
public record TagCreateRequest(
    @NotBlank(message = "태그명은 필수입니다.")
    @Schema(description = "태그명", example = "혼밥", requiredMode = Schema.RequiredMode.REQUIRED)
    String tagName
) {

    public TagCreateCommand toCommand() {
        return new TagCreateCommand(tagName);
    }
}
