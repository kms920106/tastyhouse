package com.tastyhouse.ceoapplication.region.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 행정동 계층 항목 한 건.
 *
 * <p>{@code adminDongId}·{@code code}는 {@code DONG} 레벨에서만 채워진다 — 시도·시군구는 그룹핑 이름일 뿐
 * 마스터 테이블에 자기 행이 없어 식별자가 존재하지 않는다.
 */
@Schema(description = "행정동 계층 항목 한 건")
public record AdminDongTreeItemResponse(
    @Schema(description = "표시명", example = "강남구")
    String name,

    @Schema(description = "행정동 ID(DONG 레벨에서만 채워짐)", example = "1101053")
    Long adminDongId,

    @Schema(description = "행정동 코드(DONG 레벨에서만 채워짐)", example = "1168053100")
    String code,

    @Schema(description = "하위 행정동 수(DONG 레벨에서는 1)", example = "22")
    long dongCount
) {

    public static AdminDongTreeItemResponse from(
        String name,
        Long adminDongId,
        String code,
        long dongCount
    ) {
        return new AdminDongTreeItemResponse(
            name,
            adminDongId,
            code,
            dongCount
        );
    }
}
