package com.tastyhouse.ceoapi.shop.response;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "내 가게 상세 응답")
public record ShopDetailResponse(
    @Schema(description = "가게 ID", example = "1")
    Long id,

    @Schema(description = "지하철역 ID", example = "1")
    Long stationId,

    @Schema(description = "상호명", example = "맛있는 분식")
    String name,

    @Schema(description = "위도", example = "37.497942")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.027621")
    BigDecimal longitude,

    @Schema(description = "평균 평점", example = "4.5")
    Double rating,

    @Schema(description = "도로명 주소", example = "서울시 강남구 테헤란로 1")
    String roadAddress,

    @Schema(description = "지번 주소", example = "서울시 강남구 역삼동 1")
    String lotAddress,

    @Schema(description = "대표 전화번호", example = "02-1234-5678")
    String phoneNumber,

    @Schema(description = "썸네일 이미지 파일 ID", example = "10")
    Long thumbnailImageFileId,

    @Schema(description = "상표 이미지 파일 ID", example = "11")
    Long trademarkImageFileId,

    @Schema(description = "폐업 여부", example = "false")
    boolean permanentlyClosed,

    @Schema(description = "노출정지 여부", example = "false")
    boolean hidden,

    @Schema(description = "공휴일 휴무 여부", example = "false")
    boolean closedOnPublicHolidays
) {
    public static ShopDetailResponse from(
        Long id,
        Long stationId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Double rating,
        String roadAddress,
        String lotAddress,
        String phoneNumber,
        Long thumbnailImageFileId,
        Long trademarkImageFileId,
        boolean permanentlyClosed,
        boolean hidden,
        boolean closedOnPublicHolidays
    ) {
        return new ShopDetailResponse(
            id,
            stationId,
            name,
            latitude,
            longitude,
            rating,
            roadAddress,
            lotAddress,
            phoneNumber,
            thumbnailImageFileId,
            trademarkImageFileId,
            permanentlyClosed,
            hidden,
            closedOnPublicHolidays
        );
    }
}
