package com.tastyhouse.webapplication.product.port.in;

import com.tastyhouse.webapplication.product.port.out.ProductNutritionView;

/**
 * 상품 영양정보 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code ProductNutritionQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 *
 * <p>미입력이면 {@code null}을 돌려준다 — 화면은 그때 "영양성분 보기"를 노출하지 않는다.
 */
public interface ProductNutritionQueryUseCase {

    ProductNutritionView getNutrition(Long productId);
}
