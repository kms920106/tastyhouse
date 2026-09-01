package com.tastyhouse.application.product.port.out;

import java.util.List;

import com.tastyhouse.domain.product.model.VegetarianType;

/**
 * 채식 메뉴 지정 현황 — 지정 유형·변경요청 목록·변경 가능 여부.
 *
 * <p><b>챕터 09</b>에서 신설. {@code changeable}은 도메인 서비스
 * ({@code ProductVegetarianApprovalService})가 가게 카테고리로 판정하므로 application에 남아야 하고,
 * 나머지 둘은 서로 다른 조회에서 온다. 표현 계약은 이 셋을 옮기기만 한다.
 *
 * <p>{@code vegetarianType}이 {@code null}이면 채식 메뉴가 아니다 — 해제 상태를 빈 문자열로 뭉개지
 * 않는다. 문자열 강등은 표현 계약이 수행한다.
 */
public record ProductVegetarianStatusResult(
    VegetarianType vegetarianType,
    List<ProductVegetarianRequestResult> requests,
    boolean changeable
) {
}
