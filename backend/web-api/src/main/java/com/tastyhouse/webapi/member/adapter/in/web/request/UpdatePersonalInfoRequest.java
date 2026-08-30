package com.tastyhouse.webapi.member.adapter.in.web.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.tastyhouse.webapplication.member.port.in.MemberPersonalInfoUpdateCommand;

@Schema(description = "개인정보 수정 요청")
public record UpdatePersonalInfoRequest(
    @NotNull(message = "이름은 필수입니다.")
    @Size(max = 100, message = "이름은 최대 100자까지 입력 가능합니다.")
    @Schema(description = "이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    String fullName,

    @Pattern(regexp = "^\\d{10,11}$", message = "휴대폰번호는 10~11자리 숫자여야 합니다.")
    @Schema(description = "휴대폰번호 (10~11자리 숫자)", example = "01012345678")
    String phoneNumber,

    @Schema(description = "생년월일 (YYYYMMDD)", example = "19900101")
    Integer birthDate,

    @Schema(description = "성별", example = "MALE", allowableValues = {"MALE", "FEMALE"})
    String gender,

    @Schema(description = "푸시 알림 수신 동의", example = "true")
    boolean pushNotificationEnabled,

    @Schema(description = "마케팅 정보 수신 동의", example = "false")
    boolean marketingInfoEnabled,

    @Schema(description = "이벤트 정보 수신 동의", example = "true")
    boolean eventInfoEnabled
) {

    /**
     * 인증 주체의 {@code memberId}를 주입받아 command로 변환한다.
     *
     * <p>{@code fullName}·{@code phoneNumber} 두 {@code String}과 알림 동의 3종이 각각 연달아 있어
     * 위치 기반 전달은 조용히 뒤바뀌므로, 아래는 이름 기반 접근자로 각 값을 짚어 넘긴다.
     */
    public MemberPersonalInfoUpdateCommand toCommand(Long memberId) {
        return new MemberPersonalInfoUpdateCommand(
            memberId,
            fullName,
            phoneNumber,
            birthDate,
            gender,
            pushNotificationEnabled,
            marketingInfoEnabled,
            eventInfoEnabled
        );
    }
}
