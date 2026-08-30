package com.tastyhouse.ceoapplication.region.response;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 행정동 계층 한 단계의 조회 결과.
 *
 * <p>{@code level}이 어느 단계의 목록인지 알려주므로, 화면은 요청 파라미터를 되짚지 않고 응답만 보고
 * "지금 시군구를 고르는 중"임을 알 수 있다.
 */
@Schema(description = "행정동 계층 조회 결과")
public record AdminDongTreeResponse(
    @Schema(description = "계층 단계", example = "SIGUNGU", allowableValues = {"SIDO", "SIGUNGU", "DONG"})
    String level,

    @Schema(description = "해당 단계의 항목 목록")
    List<AdminDongTreeItemResponse> items
) {

    public static AdminDongTreeResponse from(
        String level,
        List<AdminDongTreeItemResponse> items
    ) {
        return new AdminDongTreeResponse(
            level,
            items
        );
    }
}
