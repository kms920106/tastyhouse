package com.tastyhouse.domain.review.repository;

import java.util.Optional;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.review.vo.ReviewId;

/**
 * 리뷰 write 포트.
 *
 * <p>도메인 모델을 주고받는 CRUD만 남긴다(공통 지침 패턴 4). 목록·검색·통계 등 표현 목적 read는
 * infrastructure-module의 {@code ReviewQueryDao}/{@code ReviewManagementQueryDao}가 담당한다.
 *
 * <p>{@code findByIdAndMemberId}는 조회처럼 보이지만 "본인 리뷰만 수정·삭제할 수 있다"는 소유권
 * 불변식을 검증하는 command 경로 전용 로드이므로 여기 남는다.
 *
 * <p>{@code existsByOrderIdAndProductId}는 "같은 주문·같은 상품에 리뷰가 중복 생성되지 않는다"는
 * 불변식 검증용이다. REVIEW 테이블에 주문상품 단위 식별자({@code order_product_id})가 없어
 * {@code order_id}+{@code product_id} 조합으로 판정하며, 한 주문에 동일 상품을 2개 이상 담은 경우
 * 정당한 추가 리뷰까지 막히는 한계가 있다(알려진 한계로 승인됨 — 정확한 판정은 별도 컬럼 추가가 필요).
 */
public interface ReviewRepository {

    Optional<Review> findById(ReviewId reviewId);

    Optional<Review> findByIdAndMemberId(ReviewId reviewId, MemberId memberId);

    boolean existsByOrderIdAndProductId(OrderId orderId, ProductId productId);

    Review save(Review review);

    void deleteById(ReviewId reviewId);
}
