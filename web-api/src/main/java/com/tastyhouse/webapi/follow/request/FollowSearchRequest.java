package com.tastyhouse.webapi.follow.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원 검색 요청")
public record FollowSearchRequest(
    @NotBlank(message = "닉네임은 필수입니다.")
    @Schema(description = "검색할 닉네임", example = "맛집", requiredMode = Schema.RequiredMode.REQUIRED)
    String nickname
) {
}
