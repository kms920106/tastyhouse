package com.tastyhouse.domain.review.service;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.model.Review;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 리뷰 생애주기 도메인 서비스 단위 테스트. Spring/JPA 컨텍스트 없이 순수 POJO + fake write 포트로 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스 — {@code ReviewTest}와 동일한 취지).
 */
class ReviewLifecycleServiceTest {

    private ReviewLifecycleService reviewLifecycleService;

    @BeforeEach
    void setUp() {
        FakeReviewRepository reviewRepository = new FakeReviewRepository();
        reviewLifecycleService = new ReviewLifecycleService(
            reviewRepository,
            new FakeReviewImageRepository(),
            new FakeReviewTagRepository(),
            new FakeReviewLikeRepository(),
            new FakeTagRepository(),
            new FakeDomainEventPublisher()
        );
    }

    @Test
    @DisplayName("같은 주문·같은 상품에 리뷰가 없으면 등록에 성공한다")
    void register_succeedsWhenNoDuplicateForOrderAndProduct() {
        var registration = reviewLifecycleService.register(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), OrderId.of(10L),
            5, 5, 5, "맛있어요", List.of(), List.of(), false, null, null
        );

        assertThat(registration.review().getId()).isNotNull();
        assertThat(registration.review().getOrderId()).isEqualTo(OrderId.of(10L));
    }

    @Test
    @DisplayName("같은 주문·같은 상품에 이미 리뷰가 있으면 REVIEW_ALREADY_EXISTS로 등록을 차단한다")
    void register_blocksDuplicateForSameOrderAndProduct() {
        reviewLifecycleService.register(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), OrderId.of(10L),
            5, 5, 5, "맛있어요", List.of(), List.of(), false, null, null
        );

        assertThatThrownBy(() -> reviewLifecycleService.register(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), OrderId.of(10L),
            4, 4, 4, "중복 작성 테스트", List.of(), List.of(), true, null, null
        ))
            .isInstanceOf(BusinessException.class)
            .satisfies(e -> assertThat(((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.REVIEW_ALREADY_EXISTS));
    }

    @Test
    @DisplayName("사장님만보기(ownerOnly)로 작성했어도 같은 주문·상품 중복은 동일하게 차단된다")
    void register_blocksDuplicateEvenWhenExistingReviewIsOwnerOnly() {
        reviewLifecycleService.register(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), OrderId.of(157L),
            5, 5, 5, "맛있어요", List.of(), List.of(), true, null, null
        );

        assertThatThrownBy(() -> reviewLifecycleService.register(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), OrderId.of(157L),
            5, 5, 5, "중복 작성 테스트", List.of(), List.of(), true, null, null
        )).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("다른 상품이면 같은 주문이어도 중복으로 판정하지 않는다")
    void register_allowsDifferentProductUnderSameOrder() {
        reviewLifecycleService.register(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), OrderId.of(10L),
            5, 5, 5, "맛있어요", List.of(), List.of(), false, null, null
        );

        var registration = reviewLifecycleService.register(
            ShopId.of(1L), ProductId.of(99L), MemberId.of(3L), OrderId.of(10L),
            5, 5, 5, "다른 상품 리뷰", List.of(), List.of(), false, null, null
        );

        assertThat(registration.review().getProductId()).isEqualTo(ProductId.of(99L));
    }

    @Test
    @DisplayName("orderId가 없으면(주문 인증 없는 등록) 중복 검사를 생략한다")
    void register_skipsDuplicateCheckWhenOrderIdIsNull() {
        Review firstReview = reviewLifecycleService.register(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), null,
            5, 5, 5, "맛있어요", List.of(), List.of(), false, null, null
        ).review();
        assertThat(firstReview.getOrderId()).isNull();

        var secondRegistration = reviewLifecycleService.register(
            ShopId.of(1L), ProductId.of(2L), MemberId.of(3L), null,
            4, 4, 4, "또 작성", List.of(), List.of(), false, null, null
        );

        assertThat(secondRegistration.review().getId()).isNotEqualTo(firstReview.getId());
    }
}
