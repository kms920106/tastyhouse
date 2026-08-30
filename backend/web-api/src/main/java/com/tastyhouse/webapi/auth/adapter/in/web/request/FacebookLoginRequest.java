package com.tastyhouse.webapi.auth.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "페이스북 로그인 요청. Facebook JS SDK로부터 발급받은 액세스 토큰을 전달합니다.")
public record FacebookLoginRequest(

    @Schema(description = "Facebook JS SDK로부터 발급받은 액세스 토큰", example = "EAAxxxxxxx...")
    @NotBlank(message = "액세스 토큰을 입력해주세요.")
    String accessToken
) {}
