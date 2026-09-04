package com.tastyhouse.application.product.port.out;

import java.util.List;

/**
 * 메뉴 영양정보 — 영양 성분과 알레르기 유발성분 목록.
 *
 * <p><b>챕터 09</b>에서 신설. 두 값이 서로 다른 조회에서 나오므로 유스케이스가 합친다. 표현 계약이
 * {@code from(Result)} 한 번으로 끝낼 수 있게 묶는다.
 */
public record ProductNutritionViewResult(
    ProductNutritionResult nutrition,
    List<String> allergens
) {
}
