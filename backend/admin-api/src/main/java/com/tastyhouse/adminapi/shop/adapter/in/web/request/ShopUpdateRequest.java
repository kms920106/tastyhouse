package com.tastyhouse.adminapi.shop.adapter.in.web.request;

import com.tastyhouse.adminapplication.shop.port.in.ShopUpdateCommand;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "가게 수정 요청")
public record ShopUpdateRequest(
    @NotNull(message = "지하철역 ID는 필수입니다.")
    @Schema(description = "지하철역 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long stationId,

    @NotBlank(message = "상호명은 필수입니다.")
    @Schema(description = "상호명", example = "맛있는 분식", requiredMode = Schema.RequiredMode.REQUIRED)
    String name,

    @NotNull(message = "위도는 필수입니다.")
    @Schema(description = "위도", example = "37.123456", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal latitude,

    @NotNull(message = "경도는 필수입니다.")
    @Schema(description = "경도", example = "127.123456", requiredMode = Schema.RequiredMode.REQUIRED)
    BigDecimal longitude,

    @Schema(description = "도로명 주소", example = "서울시 강남구 테헤란로 1")
    String roadAddress,

    @Schema(description = "지번 주소", example = "서울시 강남구 역삼동 1-1")
    String lotAddress,

    @Schema(description = "전화번호", example = "02-1234-5678")
    String phoneNumber,

    @Schema(description = "썸네일 이미지 파일 ID", example = "10")
    Long thumbnailImageFileId
) {

    public ShopUpdateCommand toCommand(Long shopId) {
        return new ShopUpdateCommand(
            shopId, stationId, name, latitude, longitude,
            roadAddress, lotAddress, phoneNumber, thumbnailImageFileId
        );
    }
}
