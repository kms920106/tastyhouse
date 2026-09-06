package com.tastyhouse.domain.menureview.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.event.MenuReviewCreatedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewDeletedEvent;
import com.tastyhouse.domain.menureview.event.MenuReviewRatingChangedEvent;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.review.service.FakeDomainEventPublisher;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메뉴 평가 생애주기 도메인 서비스 단위 테스트.
 *
 * <p>순수 POJO이므로 Spring 컨텍스트·JPA 없이 write 포트와 이벤트 발행 포트를 fake로 대체해 검증한다.
 *
 * <p><b>가장 중요한 케이스는 {@link #register_succeedsWithoutStoreReview}다</b> — 매장 리뷰가 없어도
 * 메뉴 평가가 등록된다는 설계 원칙 1의 회귀 방어다. 이 서비스가 {@code ReviewRepository}를 아예 주입받지
 * 않는 것 자체가 그 원칙의 구조적 보증이며, 테스트는 그 상태를 봉인한다.
 */
class MenuReviewLifecycleServiceTest {

    private static final MemberId MEMBER_ID = MemberId.of(3L);
    private static final MemberId OTHER_MEMBER_ID = MemberId.of(4L);
    private static final ShopId SHOP_ID = ShopId.of(1L);
    private static final ProductId PRODUCT_ID = ProductId.of(2L);
    private static final OrderId ORDER_ID = OrderId.of(10L);
    private static final OrderProductId ORDER_PRODUCT_ID = OrderProductId.of(20L);

    private FakeMenuReviewRepository menuReviewRepository;
    private FakeDomainEventPublisher domainEventPublisher;
    private MenuReviewLifecycleService menuReviewLifecycleService;

    @BeforeEach
    void setUp() {
        menuReviewRepository = new FakeMenuReviewRepository();
        domainEventPublisher = new FakeDomainEventPublisher();
        menuReviewLifecycleService = new MenuReviewLifecycleService(menuReviewRepository, domainEventPublisher);
    }

    @Test
    @DisplayName("매장 리뷰(REVIEW)가 하나도 없어도 메뉴 평가 등록에 성공한다 — 두 평가는 독립 축이다")
    void register_succeedsWithoutStoreReview() {
        Long menuReviewId = register(ORDER_PRODUCT_ID, 5, "양념이 딱 좋았어요");

        assertThat(menuReviewId).isNotNull();
        assertThat(menuReviewRepository.findById(MenuReviewId.of(menuReviewId))).isPresent();
    }

    @Test
    @DisplayName("등록하면 MenuReviewCreatedEvent를 발행한다(상품 평점 재집계 트리거)")
    void register_publishesCreatedEvent() {
        Long menuReviewId = register(ORDER_PRODUCT_ID, 5, null);

        assertThat(domainEventPublisher.publishedEvents())
            .singleElement()
            .isInstanceOfSatisfying(MenuReviewCreatedEvent.class, event -> {
                assertThat(event.menuReviewId()).isEqualTo(MenuReviewId.of(menuReviewId));
                assertThat(event.productId()).isEqualTo(PRODUCT_ID);
                assertThat(event.shopId()).isEqualTo(SHOP_ID);
            });
    }

    @Test
    @DisplayName("같은 주문 항목에 이미 평가가 있으면 MENU_REVIEW_ALREADY_EXISTS로 차단한다")
    void register_blocksDuplicateForSameOrderProduct() {
        register(ORDER_PRODUCT_ID, 5, null);

        assertThatThrownBy(() -> register(ORDER_PRODUCT_ID, 4, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.MENU_REVIEW_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("주문 항목이 다르면 같은 상품이어도 중복으로 판정하지 않는다 — REVIEW의 '동일 상품 2건 제약'이 없다")
    void register_allowsDifferentOrderProductOfSameProduct() {
        register(ORDER_PRODUCT_ID, 5, null);

        Long secondId = register(OrderProductId.of(21L), 4, null);

        assertThat(secondId).isNotNull();
    }

    @Test
    @DisplayName("평점이 범위(1~5) 밖이면 MENU_REVIEW_NOT_ALLOWED로 거부한다")
    void register_rejectsRatingOutOfRange() {
        assertThatThrownBy(() -> register(ORDER_PRODUCT_ID, 0, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.MENU_REVIEW_NOT_ALLOWED);
        assertThatThrownBy(() -> register(ORDER_PRODUCT_ID, 6, null)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("본인 평가는 수정되고 MenuReviewRatingChangedEvent를 발행한다")
    void modify_updatesAndPublishesRatingChangedEvent() {
        Long menuReviewId = register(ORDER_PRODUCT_ID, 5, "좋아요");

        menuReviewLifecycleService.modify(MenuReviewId.of(menuReviewId), MEMBER_ID, 2, "짰어요");

        assertThat(menuReviewRepository.findById(MenuReviewId.of(menuReviewId)))
            .get()
            .satisfies(menuReview -> {
                assertThat(menuReview.getRating()).isEqualTo(2);
                assertThat(menuReview.getComment()).isEqualTo("짰어요");
            });
        assertThat(domainEventPublisher.publishedEvents())
            .last()
            .isInstanceOf(MenuReviewRatingChangedEvent.class);
    }

    @Test
    @DisplayName("타인의 평가를 수정하려 하면 MENU_REVIEW_ACCESS_DENIED로 차단한다")
    void modify_blocksOtherMembersMenuReview() {
        Long menuReviewId = register(ORDER_PRODUCT_ID, 5, null);

        assertThatThrownBy(() -> menuReviewLifecycleService.modify(MenuReviewId.of(menuReviewId), OTHER_MEMBER_ID, 1, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.MENU_REVIEW_ACCESS_DENIED);
    }

    @Test
    @DisplayName("존재하지 않는 평가를 수정하려 해도 403이다 — 404로 갈리면 존재 여부가 응답으로 새어나간다")
    void modify_missingMenuReviewIsAccessDenied() {
        assertThatThrownBy(() -> menuReviewLifecycleService.modify(MenuReviewId.of(999L), MEMBER_ID, 3, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.MENU_REVIEW_ACCESS_DENIED);
    }

    @Test
    @DisplayName("본인 평가는 삭제되고 MenuReviewDeletedEvent를 발행하며, 같은 주문 항목에 다시 평가할 수 있다")
    void remove_deletesAndAllowsReRegistration() {
        Long menuReviewId = register(ORDER_PRODUCT_ID, 5, null);

        menuReviewLifecycleService.remove(MenuReviewId.of(menuReviewId), MEMBER_ID);

        assertThat(menuReviewRepository.findById(MenuReviewId.of(menuReviewId))).isEmpty();
        assertThat(domainEventPublisher.publishedEvents())
            .last()
            .isInstanceOfSatisfying(MenuReviewDeletedEvent.class,
                event -> assertThat(event.productId()).isEqualTo(PRODUCT_ID));
        assertThat(register(ORDER_PRODUCT_ID, 3, null)).isNotNull();
    }

    @Test
    @DisplayName("타인의 평가를 삭제하려 하면 MENU_REVIEW_ACCESS_DENIED로 차단한다")
    void remove_blocksOtherMembersMenuReview() {
        Long menuReviewId = register(ORDER_PRODUCT_ID, 5, null);

        assertThatThrownBy(() -> menuReviewLifecycleService.remove(MenuReviewId.of(menuReviewId), OTHER_MEMBER_ID))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.MENU_REVIEW_ACCESS_DENIED);
    }

    private Long register(OrderProductId orderProductId, Integer rating, String comment) {
        return menuReviewLifecycleService.register(
            MEMBER_ID,
            SHOP_ID,
            PRODUCT_ID,
            ORDER_ID,
            orderProductId,
            rating,
            comment
        );
    }
}
