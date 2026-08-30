package com.tastyhouse.ceoapi.product.adapter.in.web.request;

import java.time.LocalDate;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import com.tastyhouse.ceoapplication.product.port.in.ProductExposureReplaceCommand;

/**
 * 메뉴 노출기간 전체 치환 요청.
 *
 * <p>기간·요일·시간대를 <b>replace-all</b>로 교체한다 — "요일 묶음과 개별 요일 혼용 금지"가 집합 전체를
 * 봐야 판정되는 규칙이라, 행 단위로 열면 중간 상태가 반드시 규칙을 위반한다.
 *
 * <p>{@code hours}를 빈 배열로 보내면 요일·시간 제약이 사라진다(기간 축만 남는다).
 */
@Schema(description = "메뉴 노출기간 설정 요청")
public record ProductExposureRequest(
    @NotNull(message = "가게 ID는 필수입니다.")
    @Schema(description = "대상 가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    Long shopId,

    @Schema(description = "노출 시작일. 비우면 하한이 없습니다.", example = "2026-05-01")
    LocalDate startDate,

    @Schema(description = "노출 종료일(당일 포함). 비우면 상한이 없습니다.", example = "2026-05-31")
    LocalDate endDate,

    @Valid
    @NotNull(message = "요일·시간대 목록은 필수입니다.")
    @Schema(description = "요일·시간대 목록. 빈 배열이면 요일·시간 제약이 없습니다.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    List<ProductExposureHourRequest> hours
) {

    public ProductExposureReplaceCommand toCommand(Long ceoId, Long productId) {
        return new ProductExposureReplaceCommand(
            ceoId,
            this.shopId(),
            productId,
            this.startDate(),
            this.endDate(),
            this.hours() == null ? null : this.hours().stream().map(ProductExposureHourRequest::toCommand).toList()
        );
    }
}
