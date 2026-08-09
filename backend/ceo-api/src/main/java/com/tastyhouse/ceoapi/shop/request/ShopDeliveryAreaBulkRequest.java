package com.tastyhouse.ceoapi.shop.request;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * 배달가능지역 행정동 일괄 추가·삭제 요청.
 *
 * <p>추가와 삭제가 같은 형태(행정동 식별자 목록)라 요청 record 하나를 공유한다 — 필드가 같은데 타입만
 * 나누면 이름 외에 구별 정보가 없고, 한쪽에 제약을 추가할 때 다른 쪽을 빠뜨리기 쉽다.
 *
 * <p>배열 길이 상한은 도메인 상수가 아니라 여기 Bean Validation으로 둔다 — 요청 <b>형식</b>의 제약이며,
 * 도메인이 판정하는 것은 "반영 후 총 개수"다.
 */
@Schema(description = "배달가능지역 행정동 일괄 처리 요청")
public record ShopDeliveryAreaBulkRequest(
    @NotEmpty(message = "행정동 ID 목록은 비어 있을 수 없습니다.")
    @Size(max = 500, message = "한 번에 처리할 수 있는 행정동은 최대 500개입니다.")
    @Schema(description = "행정동 ID 목록", requiredMode = Schema.RequiredMode.REQUIRED)
    List<@Positive(message = "행정동 ID는 양수여야 합니다.") Long> adminDongIds
) {
}
