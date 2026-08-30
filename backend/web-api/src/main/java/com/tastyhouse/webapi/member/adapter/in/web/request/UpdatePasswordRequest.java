package com.tastyhouse.webapi.member.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import com.tastyhouse.webapplication.member.port.in.MemberPasswordUpdateCommand;

@Schema(description = "비밀번호 변경 요청")
public record UpdatePasswordRequest(
    @NotBlank(message = "새 비밀번호를 입력해주세요.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    @Schema(description = "새 비밀번호 (8~20자)", example = "newPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    String newPassword,

    @NotBlank(message = "새 비밀번호 확인을 입력해주세요.")
    @Schema(description = "새 비밀번호 확인", example = "newPassword123!", requiredMode = Schema.RequiredMode.REQUIRED)
    String newPasswordConfirm
) {

    /**
     * 인증 주체의 {@code memberId}를 주입받아 command로 변환한다.
     *
     * <p>두 {@code String}이 연달아 있고 뒤바뀌어도 확인값 일치 검사는 대칭이라 통과해버리므로,
     * 아래는 이름 기반 접근자로 각 값을 짚어 넘긴다.
     */
    public MemberPasswordUpdateCommand toCommand(Long memberId) {
        return new MemberPasswordUpdateCommand(
            memberId,
            newPassword,
            newPasswordConfirm
        );
    }
}
