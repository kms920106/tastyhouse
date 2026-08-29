package com.tastyhouse.ceoapi.review.adapter.in.web.request;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 점주 리뷰 목록 조회 조건.
 *
 * <p>{@code startDate}/{@code endDate}의 <b>상·하한 관계</b>는 Bean Validation이 아니라 서비스가 판정한다 —
 * 두 필드에 걸친 하나의 규칙이라 어노테이션으로 쪼개면 같은 규칙 위반인데 응답 계약이 갈린다
 * ({@code ShopRequestSearchRequest} 선례와 같은 판단).
 *
 * <p>enum 후보는 도메인 enum 경계 규칙대로 {@code String}으로 받고 서비스가 승격한다.
 */
@Schema(description = "점주 리뷰 목록 조회 요청")
public record ShopReviewSearchRequest(
    @Schema(
        description = "조회 탭. 미지정 시 전체입니다. OWNER_ONLY는 작성자가 비공개(사장님만보기)로 등록한 리뷰만 조회하며, "
            + "BLINDED(게시중단)와는 독립이라 한 리뷰가 두 탭에 모두 나타날 수 있습니다.",
        allowableValues = {"ALL", "UNANSWERED", "BLINDED", "OWNER_ONLY"},
        example = "ALL"
    )
    String tab,

    @Schema(description = "조회 시작일(yyyy-MM-dd)", example = "2026-01-01")
    LocalDate startDate,

    @Schema(description = "조회 종료일(yyyy-MM-dd). 종료일 당일 작성분까지 포함합니다.", example = "2026-06-30")
    LocalDate endDate,

    @Min(value = 1, message = "별점은 1 이상이어야 합니다.")
    @Max(value = 5, message = "별점은 5 이하여야 합니다.")
    @Schema(description = "별점 필터(1~5). 내림 정수 기준이라 4는 4.0~4.9를 포함합니다.", example = "5")
    Integer rating,

    @Schema(
        description = "주문유형 필터. 미인증 리뷰(주문 정보 없음)는 어떤 값으로도 조회되지 않습니다.",
        allowableValues = {"TABLE", "RESERVATION", "DELIVERY", "TAKEOUT"},
        example = "DELIVERY"
    )
    String orderMethod,

    @Schema(description = "사진 유무 필터: 미지정=전체, true=사진 있는 리뷰만", example = "true")
    Boolean hasImage,

    @Schema(
        description = "정렬 방식. 미지정 시 저장된 기본 정렬을 적용하며, 그 설정도 없으면 최신순입니다.",
        allowableValues = {"RECOMMENDED", "LATEST", "OLDEST"},
        example = "LATEST"
    )
    String sortType
) {
}
