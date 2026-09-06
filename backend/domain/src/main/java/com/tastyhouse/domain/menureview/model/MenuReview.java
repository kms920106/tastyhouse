package com.tastyhouse.domain.menureview.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.menureview.vo.MenuReviewId;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 메뉴 평가 순수 도메인 모델 — 주문 항목 1건당 평가 1건.
 *
 * <p><b>{@code Review}(매장 리뷰)와 독립된 애그리거트다.</b> {@code reviewId}를 필드로 갖지 않으며, 두
 * 평가의 유일한 연결고리는 {@link #orderId}다. 이 설계는 "매장 평가와 메뉴 평가 중 어느 것을 먼저 하든,
 * 또는 하나만 하든 성립해야 한다"는 요구에서 나왔다 — {@code reviewId}를 두는 순간 매장 리뷰 없이 메뉴
 * 평가만 남기는 것이 구조적으로 불가능해진다. 이 불변식은 컨텍스트 경계
 * ({@code ContextBoundaryTest})가 빌드로 강제한다: review 컨텍스트의 {@code model}/{@code repository}를
 * 여기서 import하면 즉시 위반으로 드러난다.
 *
 * <p><b>의도적으로 얇다.</b> 댓글·대댓글·좋아요·사장님답변·사장님만보기가 없는 것은 누락이 아니라
 * "리뷰가 아니라 평가(rating)"라는 성격 규정이다. 소셜 기능은 전부 매장 리뷰 쪽에만 남는다.
 *
 * <p>주문 항목당 1건 제약은 {@code UNIQUE(order_product_id)}가 물리적으로 보증한다 — 애플리케이션의
 * {@code existsByOrderProductId} 검사는 사용자에게 409를 돌려주기 위한 것이고, 동시 요청의 최종 방어선은
 * 그 유니크 제약이다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code MenuReviewJpaEntity} + {@code MenuReviewMapper}가 담당하며, 변경 후 저장은 더티 체킹이 아니라
 * 명시적 {@code save} 호출이다.
 */
public class MenuReview {

    /** 평점 하한(포함). */
    private static final int MIN_RATING = 1;

    /** 평점 상한(포함). */
    private static final int MAX_RATING = 5;

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final MemberId memberId;
    private final ShopId shopId;
    private final ProductId productId;
    private final OrderId orderId;
    /** 작성 근거인 주문 항목. 불변이다 — 이미 남긴 평가의 근거를 다른 주문 항목으로 바꿀 수 없다. */
    private final OrderProductId orderProductId;
    private Integer rating;
    private String comment;
    private boolean hidden;
    private final LocalDateTime createdAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)
    private final LocalDateTime updatedAt; // DB 재구성 시에만 값 존재 (신규 생성 시 null)

    private MenuReview(
        Long id,
        MemberId memberId,
        ShopId shopId,
        ProductId productId,
        OrderId orderId,
        OrderProductId orderProductId,
        Integer rating,
        String comment,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.memberId = memberId;
        this.shopId = shopId;
        this.productId = productId;
        this.orderId = orderId;
        this.orderProductId = orderProductId;
        this.rating = rating;
        this.comment = comment;
        this.hidden = hidden;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 메뉴 평가를 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>평점 범위(1~5)를 검증한다 — 같은 검증을 {@link #updateRating}에서도 수행해 생성만 막고 변경으로
     * 뒷문이 열리는 것을 방지한다.
     */
    public static MenuReview of(
        MemberId memberId,
        ShopId shopId,
        ProductId productId,
        OrderId orderId,
        OrderProductId orderProductId,
        Integer rating,
        String comment
    ) {
        validateRating(rating);

        return new MenuReview(
            null,
            memberId,
            shopId,
            productId,
            orderId,
            orderProductId,
            rating,
            comment,
            false,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자·감사 시각을 주입한다.
     *
     * <p>{@link #of}와 달리 평점 범위를 검증하지 않는다 — 규칙 도입 이전에 저장된 행이 새 규칙을
     * 위반하더라도 로드는 가능해야 하기 때문이다({@code Product}의 가격 불변식과 같은 판단).
     */
    public static MenuReview reconstitute(
        Long id,
        MemberId memberId,
        ShopId shopId,
        ProductId productId,
        OrderId orderId,
        OrderProductId orderProductId,
        Integer rating,
        String comment,
        boolean hidden,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new MenuReview(
            id,
            memberId,
            shopId,
            productId,
            orderId,
            orderProductId,
            rating,
            comment,
            hidden,
            createdAt,
            updatedAt
        );
    }

    /**
     * 평점·코멘트를 수정한다. 작성 근거({@link #orderProductId})는 바꿀 수 없다.
     */
    public void updateRating(Integer rating, String comment) {
        validateRating(rating);

        this.rating = rating;
        this.comment = comment;
    }

    /**
     * 관리자 게시중단 — 고객 노출 목록·상품 평점 집계에서 제외된다.
     */
    public void hide() {
        this.hidden = true;
    }

    /**
     * 게시중단 해제.
     */
    public void unhide() {
        this.hidden = false;
    }

    /**
     * 평점 범위(1~5)를 검증한다. 신규 생성과 수정이 같은 검증 한 벌을 공유한다.
     */
    private static void validateRating(Integer rating) {
        if (rating == null || rating < MIN_RATING || rating > MAX_RATING) {
            throw new BusinessException(ErrorCode.MENU_REVIEW_NOT_ALLOWED,
                ErrorCode.MENU_REVIEW_NOT_ALLOWED.getDefaultMessage() + " 평점: " + rating);
        }
    }

    public Long getId() {
        return this.id;
    }

    public MenuReviewId getMenuReviewId() {
        return MenuReviewId.of(this.id);
    }

    public MemberId getMemberId() {
        return this.memberId;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public OrderProductId getOrderProductId() {
        return this.orderProductId;
    }

    public Integer getRating() {
        return this.rating;
    }

    public String getComment() {
        return this.comment;
    }

    public boolean isHidden() {
        return this.hidden;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
