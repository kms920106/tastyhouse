package com.tastyhouse.ceoapplication.ceo.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 내 로그인 이력 목록 항목 응답.
 *
 * <p>코드와 한글 라벨을 함께 내려준다 — 코드는 프론트 분기용, 라벨은 표시용이다. 라벨을 서버가
 * 내려주면 프론트에 라벨 상수를 복제하지 않아 표기 변경이 서버 배포만으로 반영된다
 * ({@code ShopChangeHistoryListItemResponse} 선례).
 */
@Schema(description = "점주 로그인 이력 목록 항목")
public record CeoLoginHistoryListItemResponse(

    @Schema(description = "이력 ID", example = "1024")
    Long id,

    @Schema(description = "로그인 결과 코드", example = "SUCCESS", allowableValues = {"SUCCESS", "FAILURE"})
    String result,

    @Schema(description = "로그인 결과 한글 라벨", example = "로그인 성공")
    String resultName,

    @Schema(
        description = "실패 사유 코드. 성공 시 null",
        example = "BAD_CREDENTIALS",
        allowableValues = {"BAD_CREDENTIALS", "ACCOUNT_INACTIVE"}
    )
    String failureReason,

    @Schema(description = "실패 사유 한글 라벨. 성공 시 null", example = "비밀번호 불일치")
    String failureReasonName,

    @Schema(description = "접속 IP. 판별 불가 시 null", example = "121.130.11.24")
    String ipAddress,

    @Schema(description = "접속 기기 정보(User-Agent). 미전송 시 null", example = "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X)")
    String userAgent,

    @Schema(description = "로그인 시각", example = "2026-08-14T09:12:41")
    LocalDateTime loggedInAt
) {

    public static CeoLoginHistoryListItemResponse from(
        Long id,
        String result,
        String resultName,
        String failureReason,
        String failureReasonName,
        String ipAddress,
        String userAgent,
        LocalDateTime loggedInAt
    ) {
        return new CeoLoginHistoryListItemResponse(
            id,
            result,
            resultName,
            failureReason,
            failureReasonName,
            ipAddress,
            userAgent,
            loggedInAt
        );
    }
}
