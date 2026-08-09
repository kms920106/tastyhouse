package com.tastyhouse.ceoapi.region.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 행정동 계층 조회 요청.
 *
 * <p>두 파라미터가 모두 비면 시도 목록, {@code sidoName}만 있으면 그 시도의 시군구 목록, 둘 다 있으면
 * 행정동 목록을 조회한다 — 요청 한 형태로 3단을 모두 표현한다.
 *
 * <p>{@code sigunguName}만 단독으로 오는 것은 상위 계층 없이 하위를 특정하려는 것이라 성립하지 않는다
 * (같은 이름의 시군구가 여러 시도에 존재한다 — 예: "중구"). 이 검증은 서비스가 수행한다.
 */
@Schema(description = "행정동 계층 조회 요청")
public record AdminDongTreeRequest(
    @Schema(description = "시/도 이름. 비우면 시/도 목록을 조회", example = "서울특별시")
    String sidoName,

    @Schema(description = "시/군/구 이름. sidoName과 함께 지정하면 행정동 목록을 조회", example = "강남구")
    String sigunguName
) {
}
