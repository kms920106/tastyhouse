package com.tastyhouse.adminapi.admin.adapter.in.web.request;

import com.tastyhouse.adminapi.admin.application.port.in.AdminCreateCommand;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "관리자 계정 생성 요청 (SUPER_ADMIN 전용)")
public record AdminCreateRequest(
    @NotBlank(message = "아이디를 입력해주세요.")
    @Size(min = 4, max = 50, message = "아이디는 4~50자여야 합니다.")
    @Schema(description = "관리자 아이디", example = "manager01")
    String username,

    @NotBlank(message = "비밀번호를 입력해주세요.")
    @Size(min = 8, max = 100, message = "비밀번호는 8자 이상이어야 합니다.")
    @Schema(description = "비밀번호", example = "password123!")
    String password,

    @NotBlank(message = "이름을 입력해주세요.")
    @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
    @Schema(description = "관리자 이름", example = "홍길동")
    String name,

    @NotBlank(message = "권한을 선택해주세요.")
    @Schema(description = "관리자 권한", example = "ADMIN", allowableValues = {"SUPER_ADMIN", "ADMIN"}, requiredMode = Schema.RequiredMode.REQUIRED)
    String role
) {

    public AdminCreateCommand toCommand() {
        return new AdminCreateCommand(username(), password(), name(), role());
    }
}
