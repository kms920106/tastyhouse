package com.tastyhouse.adminapi.rank.adapter.in.web.response;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.rank.port.out.RankPrizeManagementResult;

import com.tastyhouse.adminapi.common.response.FileResponse;

@Schema(description = "랭킹 경품 상세 응답")
public record RankPrizeDetailResponse(
    @Schema(description = "경품 ID", example = "10")
    Long id,

    @Schema(description = "소속 기간 ID", example = "1")
    Long periodId,

    @Schema(description = "등수", example = "1")
    Integer prizeRank,

    @Schema(description = "경품명", example = "아이패드 Pro 11인치")
    String name,

    @Schema(description = "브랜드", example = "Apple")
    String brand,

    @Schema(description = "경품 이미지 파일 정보")
    FileResponse image
) {

    public static RankPrizeDetailResponse from(RankPrizeManagementResult result) {
        return new RankPrizeDetailResponse(
            result.id(),
            result.periodId(),
            result.prizeRank(),
            result.name(),
            result.brand(),
            toFileResponse(result)
        );
    }

    private static FileResponse toFileResponse(RankPrizeManagementResult result) {
        if (result.imageFileId() == null) {
            return null;
        }
        return FileResponse.of(result.imageFileId(), result.imageFileName(), result.imageUrl());
    }
}
