package com.tastyhouse.ceoapi.auth.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import com.tastyhouse.application.auth.port.in.CeoAuthLoginCommand;

@Schema(description = "점주 로그인 요청")
public record LoginRequest(
    @NotBlank(message = "아이디를 입력해주세요.")
    @Schema(description = "점주 아이디", example = "ceo")
    String username,

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Schema(description = "비밀번호", example = "password123!")
    String password,

    @Schema(description = "로그인 상태 유지 여부 (true: 30일, false: 7일)", example = "false", defaultValue = "false")
    boolean rememberMe
) {

    /**
     * 접속기록에 남길 {@code ipAddress}·{@code userAgent}는 본문이 아니라 서블릿 요청에서 나오므로
     * 컨트롤러가 뽑아 넘긴다 — Command는 경계 타입만 싣고 서블릿을 알지 않는다.
     */
    public CeoAuthLoginCommand toCommand(String ipAddress, String userAgent) {
        return CeoAuthLoginCommand.of(username, password, rememberMe, ipAddress, userAgent);
    }
}
