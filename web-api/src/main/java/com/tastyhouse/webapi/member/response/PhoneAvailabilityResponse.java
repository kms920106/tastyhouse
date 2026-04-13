package com.tastyhouse.webapi.member.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "휴대폰번호 가입 가능 여부 확인 응답")
public record PhoneAvailabilityResponse(
    @Schema(description = "휴대폰번호 가입 가능 여부 (true: 가입 가능, false: 이미 가입된 번호)", example = "true")
    boolean available
) {
    public static PhoneAvailabilityResponse from(boolean available) {
        return new PhoneAvailabilityResponse(available);
    }
}
