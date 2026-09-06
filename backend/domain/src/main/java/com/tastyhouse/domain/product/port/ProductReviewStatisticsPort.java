package com.tastyhouse.domain.product.port;

/**
 * 상품 메뉴 평가 통계 조회 출력 포트.
 *
 * <p>메뉴 평가가 등록·수정·삭제되면 해당 상품의 평균 평점과 평가 수를 다시 계산해 상품에 반영해야 한다.
 * 그 재계산에 필요한 집계는 QueryDSL 조회라 도메인이 직접 알 수 없으므로, 이 포트를 도메인에 두고
 * infrastructure-module의 {@code ProductReviewStatisticsAdapter}가
 * {@code MenuReviewStatisticsQueryDao}에 위임해 구현한다.
 *
 * <p><b>{@code PRODUCT.rating}의 근거는 MENU_REVIEW뿐이다(이관 완료).</b> 과거에는 REVIEW의
 * 맛·양·가격 평점 3종을 부분 평균해 썼고 이 포트도 그에 맞춰 메서드가 4개였으나, 맛·양·가격은 <b>매장
 * 리뷰의 항목별 평점</b>이지 메뉴 평가의 축이 아니다. MENU_REVIEW는 {@code rating} 하나뿐이므로 포트도
 * 그에 맞춰 2개로 줄였다.
 *
 * <p>고객 노출 조건은 {@code hidden = false} 하나뿐이다 — MENU_REVIEW에는 사장님만보기 개념이 없다.
 * 그 결과 "사장님만보기 리뷰가 상품 대표 평점으로 새어나간다"는 과거의 위험은 <b>구조적으로 사라졌다</b>.
 *
 * <p>대상 평가가 없으면 평균값은 {@code null}이다.
 */
public interface ProductReviewStatisticsPort {

    Long countVisibleMenuReviewsByProductId(Long productId);

    Double getAverageMenuRatingByProductId(Long productId);
}
