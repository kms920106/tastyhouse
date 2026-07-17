package com.tastyhouse.webapi.reservation.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "예약 상세 조회 응답")
public record ReservationDetailResponse(
    @Schema(description = "예약 ID", example = "1")
    Long id,

    @Schema(description = "가게 ID", example = "1")
    Long shopId,

    @Schema(description = "가게명", example = "BBQ치킨 성내점")
    String shopName,

    @Schema(description = "가게 썸네일 이미지 URL", example = "https://cdn.tastyhouse.com/shop/1/thumbnail.jpg")
    String shopImageUrl,

    @Schema(description = "가게 도로명 주소", example = "서울특별시 강남구 테헤란로 123")
    String shopRoadAddress,

    @Schema(description = "가게 지번 주소", example = "서울특별시 강남구 역삼동 123-45")
    String shopLotAddress,

    @Schema(description = "예약자 회원 ID", example = "2")
    Long memberId,

    @Schema(description = "예약자 이름", example = "테이스티하우스")
    String reserverName,

    @Schema(description = "예약자 휴대폰 번호", example = "01011111111")
    String reserverPhoneNumber,

    @Schema(description = "예약자 이메일", example = "tastyhouse20@gmail.com")
    String reserverEmail,

    @Schema(description = "예약 일시", example = "2026-06-10T13:00:00")
    LocalDateTime reservationAt,

    @Schema(description = "방문 인원수", example = "4")
    Integer partySize,

    @Schema(description = "예약 상태", example = "CONFIRMED")
    String status,

    @Schema(description = "요청사항", example = "창가 자리 부탁드립니다")
    String request,

    @Schema(description = "예약 생성 일시", example = "2026-06-03T10:30:00")
    LocalDateTime createdAt
) {
    public static ReservationDetailResponse from(
        Long id,
        Long shopId,
        String shopName,
        String shopImageUrl,
        String shopRoadAddress,
        String shopLotAddress,
        Long memberId,
        String reserverName,
        String reserverPhoneNumber,
        String reserverEmail,
        LocalDateTime reservationAt,
        Integer partySize,
        String status,
        String request,
        LocalDateTime createdAt
    ) {
        return new ReservationDetailResponse(
            id,
            shopId,
            shopName,
            shopImageUrl,
            shopRoadAddress,
            shopLotAddress,
            memberId,
            reserverName,
            reserverPhoneNumber,
            reserverEmail,
            reservationAt,
            partySize,
            status,
            request,
            createdAt
        );
    }
}
