package com.tastyhouse.ceoapplication.region.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 행정동 경계 조회 결과.
 *
 * <p>{@code truncated}가 {@code true}면 요청 영역이 너무 넓어 경계를 내려보내지 않은 것이다 — 전국이
 * 보이는 줌 레벨에서 3,600개 동의 경계를 전송하면 응답이 수십 MB가 된다. 400으로 거절하지 않고 빈 배열로
 * 응답하는 이유는, 지도를 축소하는 것이 <b>오류가 아니라 정상 조작</b>이기 때문이다. 화면은 이 플래그를
 * 보고 "확대하면 경계가 표시됩니다"를 안내한다.
 */
@Schema(description = "행정동 경계 조회 결과")
public record AdminDongBoundaryResponse(
    @Schema(description = "조회 영역이 너무 넓어 경계를 생략했는지", example = "false")
    boolean truncated,

    @Schema(description = "행정동 경계 목록")
    List<AdminDongBoundaryItemResponse> items
) {

    public static AdminDongBoundaryResponse from(
        boolean truncated,
        List<AdminDongBoundaryItemResponse> items
    ) {
        return new AdminDongBoundaryResponse(
            truncated,
            items
        );
    }
}
