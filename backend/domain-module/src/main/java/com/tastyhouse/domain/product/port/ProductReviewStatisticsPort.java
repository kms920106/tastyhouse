package com.tastyhouse.domain.product.port;

/**
 * 상품 리뷰 통계 조회 출력 포트.
 *
 * <p>리뷰가 등록·삭제되면 해당 상품의 평균 평점과 리뷰 수를 다시 계산해 상품에 반영해야 한다. 그 재계산에
 * 필요한 집계는 리뷰 테이블에 대한 QueryDSL 조회라 도메인이 직접 알 수 없으므로, 이 포트를 도메인에 두고
 * infrastructure-module의 어댑터가 {@code ReviewQueryDao}에 위임해 구현한다
 * ({@code rank} 도메인의 {@code MemberReviewCountPort}와 같은 형태).
 *
 * <p>이 포트는 review 도메인 전환(30-review)에서 write 포트를 순수화하며 신설했다. 과거
 * {@code ProductReviewEventListener}가 {@code ReviewRepository}의 통계 read 메서드를 직접 호출했는데,
 * 그 메서드들이 write 포트에서 제거되면서 대체가 필요했기 때문이다. 리스너 자체의 위치 개편
 * (infrastructure {@code product/listener/}로 이동)은 32-product 소관이며, 그때 이 포트를 그대로 쓰거나
 * infra 안에서 DAO를 직접 주입하도록 정리하면 된다.
 *
 * <p>모든 집계는 <b>고객에게 노출되는 리뷰</b>(숨김·사장님만보기 제외)만 대상으로 한다. 특히 사장님만보기
 * 리뷰가 여기 섞이면 {@code PRODUCT}의 비정규화 평점 컬럼을 통해 <b>상품 대표 평점으로 전 화면에 새어나가</b>
 * 비공개 정책을 정면으로 위반하므로, 어댑터가 위임하는 DAO에서 반드시 두 축을 함께 건다.
 *
 * <p>대상 리뷰가 없으면 평균값은 {@code null}이다.
 */
public interface ProductReviewStatisticsPort {

    Long countVisibleReviewsByProductId(Long productId);

    Double getAverageTasteRatingByProductId(Long productId);

    Double getAverageAmountRatingByProductId(Long productId);

    Double getAveragePriceRatingByProductId(Long productId);
}
