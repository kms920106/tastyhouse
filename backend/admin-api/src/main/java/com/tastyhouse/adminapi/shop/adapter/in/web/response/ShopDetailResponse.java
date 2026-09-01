package com.tastyhouse.adminapi.shop.adapter.in.web.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

import com.tastyhouse.application.shop.port.out.ShopManagementDetailResult;

@Schema(description = "가게 상세 응답")
public record ShopDetailResponse(
    @Schema(description = "가게 ID", example = "1")
    Long id,

    @Schema(description = "지하철역 ID", example = "1")
    Long stationId,

    @Schema(description = "상호명", example = "맛있는 분식")
    String name,

    @Schema(description = "위도", example = "37.123456")
    BigDecimal latitude,

    @Schema(description = "경도", example = "127.123456")
    BigDecimal longitude,

    @Schema(description = "평균 평점", example = "4.5")
    Double rating,

    @Schema(description = "도로명 주소", example = "서울시 강남구 테헤란로 1")
    String roadAddress,

    @Schema(description = "지번 주소", example = "서울시 강남구 역삼동 1-1")
    String lotAddress,

    @Schema(description = "전화번호", example = "02-1234-5678")
    String phoneNumber,

    @Schema(description = "썸네일 이미지 URL(없으면 null)", example = "https://firebasestorage.googleapis.com/v0/b/bucket/o/2025%2F02%2F16%2Fthumb.jpg?alt=media")
    String thumbnailImageUrl,

    @Schema(description = "폐업 여부", example = "false")
    boolean permanentlyClosed,

    @Schema(description = "일회용컵 보증금제 대상 사업자 여부. true여야 이 가게가 보증금 옵션그룹을 "
        + "만들 수 있습니다. PATCH /api/shops/v1/{id}/cup-deposit 로 변경합니다.", example = "false")
    boolean cupDepositEnabled,

    @Schema(description = "생성일시", example = "2026-01-01T00:00:00")
    LocalDateTime createdAt,

    @Schema(description = "수정일시", example = "2026-01-02T00:00:00")
    LocalDateTime updatedAt
) {
    /**
     * <p>썸네일 URL은 {@code ShopManagementDetailResult}에 없다 — 가게 상세와 이미지가 서로 다른 읽기
     * 포트에 있어 QueryService가 두 번 조회해 합친다. 그래서 이 팩토리만 Result 한 개가 아니라
     * 조회된 URL을 별도 인자로 받는다(미등록이면 {@code null}).
     */
    public static ShopDetailResponse from(ShopManagementDetailResult result, String thumbnailImageUrl) {
        return new ShopDetailResponse(
            result.id(),
            result.stationId(),
            result.name(),
            result.latitude(),
            result.longitude(),
            result.rating(),
            result.roadAddress(),
            result.lotAddress(),
            result.phoneNumber(),
            thumbnailImageUrl,
            result.permanentlyClosed(),
            result.cupDepositEnabled(),
            result.createdAt(),
            result.updatedAt()
        );
    }
}
