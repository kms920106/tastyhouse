package com.tastyhouse.ceoapplication.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 메뉴 상세(점주 관리 화면). 노출기간 상세값(요일·시간대·기간)·이미지·연결된 옵션그룹은 각각 별도
 * 조회 API(§6·§7·§5-2)가 담당하므로 이 응답에는 담지 않는다. 다만 {@code exposureScheduled}만은
 * 예외로 포함한다 — 화면이 §6을 열기 전(최초 렌더·새로고침)에도 "노출기간 설정됨" 요약을 보여줘야
 * 하기 때문이다.
 */
@Schema(description = "메뉴 상세")
public record ProductDetailResponse(
    @Schema(description = "메뉴 ID", example = "108")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "메뉴그룹 ID. 미분류면 null", example = "10")
    Long productCategoryId,

    @Schema(description = "메뉴그룹명. 미분류면 null", example = "인기 메뉴")
    String productCategoryName,

    @Schema(description = "메뉴명", example = "후라이드 치킨")
    String name,

    @Schema(description = "구성. 미설정이면 null", example = "후라이드 치킨 1마리")
    String composition,

    @Schema(description = "메뉴 설명. 미설정이면 null", example = "바삭한 후라이드 치킨")
    String description,

    @Schema(description = "정가", example = "18000")
    Integer originalPrice,

    @Schema(description = "할인가. 미설정이면 null", example = "16000")
    Integer discountPrice,

    @Schema(description = "1인분 여부", example = "false")
    boolean singleServing,

    @Schema(description = "맵기 단계. 미설정이면 null", example = "1")
    Integer spiciness,

    @Schema(description = "사장님 추천 여부", example = "false")
    boolean representative,

    @Schema(description = "평가 제외 여부", example = "false")
    boolean ratingExcluded,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "대표 이미지 URL. 등록된 이미지가 없으면 null")
    String imageUrl,

    @Schema(description = "채식 단계. 채식 메뉴가 아니면 null", example = "LACTO_OVO",
        allowableValues = {"VEGAN", "LACTO", "OVO", "LACTO_OVO", "PESCO"})
    String vegetarianType,

    @Schema(description = "중량 표기(치킨 등 법정 의무표시 대상). 미표시면 null", example = "조리 전 총 중량 1,200g")
    String weightText,

    @Schema(description = "노출기간(요일·시간대 또는 기간)이 설정되어 있는지 여부. 상세 화면 새로고침 직후" +
        " 초기 요약 표시에 쓰인다 — 정확한 노출기간 값은 별도 조회 API(§6)가 담당", example = "false")
    boolean exposureScheduled
) {

    public static ProductDetailResponse from(
        Long id,
        Long shopId,
        Long productCategoryId,
        String productCategoryName,
        String name,
        String composition,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        boolean singleServing,
        Integer spiciness,
        boolean representative,
        boolean ratingExcluded,
        boolean soldOut,
        boolean visible,
        String imageUrl,
        String vegetarianType,
        String weightText,
        boolean exposureScheduled
    ) {
        return new ProductDetailResponse(
            id,
            shopId,
            productCategoryId,
            productCategoryName,
            name,
            composition,
            description,
            originalPrice,
            discountPrice,
            singleServing,
            spiciness,
            representative,
            ratingExcluded,
            soldOut,
            visible,
            imageUrl,
            vegetarianType,
            weightText,
            exposureScheduled
        );
    }
}
