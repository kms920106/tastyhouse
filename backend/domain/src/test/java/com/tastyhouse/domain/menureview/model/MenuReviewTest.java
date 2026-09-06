package com.tastyhouse.domain.menureview.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 메뉴 평가 도메인 모델 순수 단위 테스트.
 *
 * <p>평점 범위 검증이 <b>팩토리와 전이 양쪽</b>에 걸려 있는지가 핵심이다 — 생성만 막고 수정을 열어두면
 * 같은 위반 값이 곧바로 뒷문으로 들어온다.
 */
class MenuReviewTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 숨김 처리되지 않은 상태다")
    void of_createsTransientMenuReview() {
        MenuReview menuReview = menuReview(5, "양념이 딱 좋았어요");

        assertThat(menuReview.getId()).isNull();
        assertThat(menuReview.getMemberId()).isEqualTo(MemberId.of(3L));
        assertThat(menuReview.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(menuReview.getProductId()).isEqualTo(ProductId.of(2L));
        assertThat(menuReview.getOrderId()).isEqualTo(OrderId.of(10L));
        assertThat(menuReview.getOrderProductId()).isEqualTo(OrderProductId.of(20L));
        assertThat(menuReview.getRating()).isEqualTo(5);
        assertThat(menuReview.getComment()).isEqualTo("양념이 딱 좋았어요");
        assertThat(menuReview.isHidden()).isFalse();
        assertThat(menuReview.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("of는 코멘트가 없어도(선택 항목) 생성된다")
    void of_allowsNullComment() {
        assertThat(menuReview(4, null).getComment()).isNull();
    }

    @Test
    @DisplayName("of는 평점 경계값 1·5를 허용한다")
    void of_allowsRatingBoundaries() {
        assertThat(menuReview(1, null).getRating()).isEqualTo(1);
        assertThat(menuReview(5, null).getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("of는 평점이 0·6·null이면 MENU_REVIEW_NOT_ALLOWED로 거부한다")
    void of_rejectsRatingOutOfRange() {
        assertThatThrownBy(() -> menuReview(0, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.MENU_REVIEW_NOT_ALLOWED);
        assertThatThrownBy(() -> menuReview(6, null)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> menuReview(null, null)).isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("updateRating은 평점·코멘트를 변경하되 작성 근거(orderProductId)는 유지한다")
    void updateRating_changesRatingAndCommentOnly() {
        MenuReview menuReview = menuReview(5, "좋아요");

        menuReview.updateRating(2, "생각보다 짰어요");

        assertThat(menuReview.getRating()).isEqualTo(2);
        assertThat(menuReview.getComment()).isEqualTo("생각보다 짰어요");
        assertThat(menuReview.getOrderProductId()).isEqualTo(OrderProductId.of(20L));
    }

    @Test
    @DisplayName("updateRating도 평점 범위를 검증한다(생성만 막고 수정을 열어두지 않는다)")
    void updateRating_rejectsRatingOutOfRange() {
        MenuReview menuReview = menuReview(5, null);

        assertThatThrownBy(() -> menuReview.updateRating(6, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.MENU_REVIEW_NOT_ALLOWED);
        assertThat(menuReview.getRating()).isEqualTo(5);
    }

    @Test
    @DisplayName("hide/unhide는 숨김 플래그를 전환한다")
    void hideUnhide_togglesHidden() {
        MenuReview menuReview = menuReview(5, null);

        menuReview.hide();
        assertThat(menuReview.isHidden()).isTrue();

        menuReview.unhide();
        assertThat(menuReview.isHidden()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각·숨김을 포함해 재구성하며 평점 범위를 검증하지 않는다")
    void reconstitute_restoresPersistedStateWithoutValidation() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);

        MenuReview menuReview = MenuReview.reconstitute(
            77L,
            MemberId.of(3L),
            ShopId.of(1L),
            ProductId.of(2L),
            OrderId.of(10L),
            OrderProductId.of(20L),
            9,
            "레거시 값",
            true,
            createdAt,
            createdAt
        );

        assertThat(menuReview.getId()).isEqualTo(77L);
        assertThat(menuReview.getMenuReviewId()).isEqualTo(MenuReviewId.of(77L));
        assertThat(menuReview.getRating()).isEqualTo(9);
        assertThat(menuReview.isHidden()).isTrue();
        assertThat(menuReview.getCreatedAt()).isEqualTo(createdAt);
    }

    private MenuReview menuReview(Integer rating, String comment) {
        return MenuReview.of(
            MemberId.of(3L),
            ShopId.of(1L),
            ProductId.of(2L),
            OrderId.of(10L),
            OrderProductId.of(20L),
            rating,
            comment
        );
    }
}
