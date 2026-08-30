package com.tastyhouse.ceoapplication.product.response;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 품절·숨김 관리 옵션 항목.
 *
 * <p>{@code optionType}이 항목마다 필요한 이유: 일반 옵션과 공통 옵션은 <b>다른 테이블·다른 id 시퀀스</b>라
 * id만으로는 어느 쪽인지 알 수 없다. 일괄 처리 요청에 이 값을 함께 실어야 서버가 올바른 리포지토리를 고른다.
 */
@Schema(description = "품절·숨김 관리 옵션 항목")
public record ProductOptionAvailabilityItemResponse(
    @Schema(description = "옵션 ID", example = "100")
    Long id,

    @Schema(description = "옵션 종류. 일괄 처리 요청 시 이 값을 함께 보낸다.", example = "NORMAL",
        allowableValues = {"NORMAL", "COMMON"})
    String optionType,

    @Schema(description = "옵션명", example = "곱빼기")
    String name,

    @Schema(description = "추가 금액", example = "1000")
    Integer additionalPrice,

    @Schema(description = "품절 여부", example = "false")
    boolean soldOut,

    @Schema(description = "품절 자동해제 시각. 무기한 품절이거나 판매중이면 null", example = "2026-08-18T09:00:00")
    LocalDateTime soldOutUntil,

    @Schema(description = "노출 여부", example = "true")
    boolean visible,

    @Schema(description = "정렬 순서", example = "1")
    Integer sort
) {

    public static ProductOptionAvailabilityItemResponse from(
        Long id,
        String optionType,
        String name,
        Integer additionalPrice,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer sort
    ) {
        return new ProductOptionAvailabilityItemResponse(
            id,
            optionType,
            name,
            additionalPrice,
            soldOut,
            soldOutUntil,
            visible,
            sort
        );
    }
}
