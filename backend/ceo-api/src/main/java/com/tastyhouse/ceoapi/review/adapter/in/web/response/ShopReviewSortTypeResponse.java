package com.tastyhouse.ceoapi.review.adapter.in.web.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.review.port.out.ShopReviewSortTypeView;

@Schema(description = "리뷰 정렬 설정 조회 응답")
public record ShopReviewSortTypeResponse(
    @Schema(
        description = "적용 중인 기본 정렬. 미설정 가게는 기본값 LATEST가 내려갑니다.",
        allowableValues = {"RECOMMENDED", "LATEST", "OLDEST"},
        example = "LATEST"
    )
    String sortType,

    @Schema(description = "정렬 방식 한글명", example = "최신순")
    String sortTypeDescription,

    @Schema(description = "설정 최종 변경일시. 한 번도 설정하지 않았으면 null입니다.", example = "2026-06-18T11:20:00")
    LocalDateTime updatedAt
) {

    public static ShopReviewSortTypeResponse from(ShopReviewSortTypeView view) {
        return new ShopReviewSortTypeResponse(
            view.sortType(),
            view.sortTypeDescription(),
            view.updatedAt()
        );
    }
}
