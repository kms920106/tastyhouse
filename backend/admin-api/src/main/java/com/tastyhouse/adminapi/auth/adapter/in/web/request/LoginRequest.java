package com.tastyhouse.adminapi.auth.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import com.tastyhouse.adminapplication.auth.port.in.AdminAuthLoginCommand;

@Schema(description = "관리자 로그인 요청")
public record LoginRequest(
    @NotBlank(message = "아이디를 입력해주세요.")
    @Schema(description = "관리자 아이디", example = "admin")
    String username,

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Schema(description = "비밀번호", example = "password123!")
    String password,

    @Schema(description = "로그인 상태 유지 여부 (true: 30일, false: 7일)", example = "false", defaultValue = "false")
    boolean rememberMe
) {

    public AdminAuthLoginCommand toCommand() {
        return AdminAuthLoginCommand.of(username, password, rememberMe);
    }
}
