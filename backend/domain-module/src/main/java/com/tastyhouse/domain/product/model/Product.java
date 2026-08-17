package com.tastyhouse.domain.product.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.tastyhouse.domain.product.vo.ProductCategoryId;
import com.tastyhouse.domain.product.vo.ProductDiscountInfo;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

/**
 * 상품 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductJpaEntity} + {@code ProductMapper}가 담당한다. 도메인이 프레임워크-프리이므로
 * 변경 후 저장은 더티 체킹이 아니라 command 서비스가 명시적으로 {@code ProductRepository#save}를
 * 호출해야 한다.
 */
public class Product {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ShopId shopId;
    private ProductCategoryId productCategoryId;
    private String name;
    private String description;
    private Integer originalPrice;
    private ProductDiscountInfo discountInfo;
    private Double rating;
    private Integer reviewCount;
    private boolean representative;
    private Integer spiciness;
    private boolean soldOut;
    /**
     * 품절 자동해제 시각. {@code null}이면 수동 해제까지 유지되는 무기한 품절이다.
     *
     * <p>{@code soldOut}이 품절 상태의 진실원이고 이 필드는 "언제 자동으로 풀리는가"만 담는다 —
     * 무기한 품절을 표현할 수 있어야 하고, 기간 개념이 없는 기존 admin 경로가 그대로 성립해야 하므로
     * {@code soldOut}을 {@code soldOutUntil != null}로 대체하지 않는다.
     */
    private LocalDateTime soldOutUntil;
    private boolean visible;
    private Integer sort;
    /**
     * 메뉴 평가 제외 여부(주류·사이드 등). {@code PRODUCT_CATEGORY.name}이 가게별 자유 입력 문자열이라
     * 주류·사이드를 기계 판별할 수 없어 상품에 명시 플래그를 둔다.
     *
     * <p>ceo 메뉴 관리 화면에서 토글하는 값이며 상품 수정 경로가 아직 이 필드를 다루지 않으므로
     * {@code final}이다 — 전이 메서드를 두지 않는 것이 "현재는 변경 경로가 없다"의 구조적 표현이다.
     */
    private final boolean ratingExcluded;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Product(
        Long id,
        ShopId shopId,
        ProductCategoryId productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        ProductDiscountInfo discountInfo,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer sort,
        boolean ratingExcluded,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.shopId = shopId;
        this.productCategoryId = productCategoryId;
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountInfo = discountInfo;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.representative = representative;
        this.spiciness = spiciness;
        this.soldOut = soldOut;
        this.soldOutUntil = soldOutUntil;
        this.visible = visible;
        this.sort = sort;
        this.ratingExcluded = ratingExcluded;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 신규 상품을 생성한다. 아직 영속되지 않았으므로 식별자·감사 시각은 없다.
     *
     * <p>가격 불변식({@link #validatePrices})을 강제한다 — 정가·할인가 음수 금지, 할인가 &lt;= 정가.
     *
     * <p>{@link #reconstitute}는 이 검증을 <b>거치지 않는다</b> — 기존 DB 데이터가 새 불변식을 위반해도
     * 로드는 가능해야 하기 때문이다.
     */
    public static Product of(
        ShopId shopId,
        ProductCategoryId productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer sort,
        boolean ratingExcluded
    ) {
        validatePrices(originalPrice, discountPrice);

        return new Product(
            null,
            shopId,
            productCategoryId,
            name,
            description,
            originalPrice,
            ProductDiscountInfo.of(discountPrice, discountRate),
            rating,
            reviewCount != null ? reviewCount : 0,
            representative,
            spiciness,
            soldOut,
            soldOutUntil,
            visible,
            sort,
            ratingExcluded,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     *
     * <p><b>{@link #of}와 달리 가격 불변식 검증을 하지 않는다</b> — 불변식 도입 이전에 저장된 기존 상품이
     * 새 규칙을 위반하더라도 로드는 가능해야 하기 때문이다.
     */
    public static Product reconstitute(
        Long id,
        ShopId shopId,
        ProductCategoryId productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        ProductDiscountInfo discountInfo,
        Double rating,
        Integer reviewCount,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        LocalDateTime soldOutUntil,
        boolean visible,
        Integer sort,
        boolean ratingExcluded,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Product(
            id,
            shopId,
            productCategoryId,
            name,
            description,
            originalPrice,
            discountInfo,
            rating,
            reviewCount,
            representative,
            spiciness,
            soldOut,
            soldOutUntil,
            visible,
            sort,
            ratingExcluded,
            createdAt,
            updatedAt
        );
    }

    public Long getId() {
        return this.id;
    }

    public ShopId getShopId() {
        return this.shopId;
    }

    public ProductCategoryId getProductCategoryId() {
        return this.productCategoryId;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Integer getOriginalPrice() {
        return this.originalPrice;
    }

    public ProductDiscountInfo getDiscountInfo() {
        return this.discountInfo;
    }

    public Double getRating() {
        return this.rating;
    }

    public Integer getReviewCount() {
        return this.reviewCount;
    }

    public boolean isRepresentative() {
        return this.representative;
    }

    public Integer getSpiciness() {
        return this.spiciness;
    }

    public boolean isSoldOut() {
        return this.soldOut;
    }

    public LocalDateTime getSoldOutUntil() {
        return this.soldOutUntil;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public Integer getSort() {
        return this.sort;
    }

    /**
     * 메뉴 평가 제외 여부 — {@code true}면 이 상품에는 메뉴 평가를 남길 수 없고, 평가 가능 메뉴 목록에도
     * 담기지 않는다. 판정은 서버가 하며 프론트가 카테고리 이름으로 거르지 않는다.
     */
    public boolean isRatingExcluded() {
        return this.ratingExcluded;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public ProductId getProductId() {
        return ProductId.of(this.id);
    }

    /**
     * 가격 불변식을 검증한다 — 신규 생성({@code of})과 변경({@code update}) 양쪽이 같은 검증 한 벌을
     * 공유한다. 생성만 막고 변경을 열어두면 같은 위반 값이 곧바로 뒷문으로 들어오기 때문이다.
     *
     * <p>검증 항목: {@code originalPrice} 음수 금지, {@code discountPrice} 음수 금지,
     * {@code discountPrice <= originalPrice}. {@code discountPrice}가 null이면 "할인 없음"이므로
     * 비교 대상에서 제외한다.
     *
     * <p>{@code originalPrice}가 null인 경우는 여기서 막지 않는다 — 기존 호출부가 필수값으로 보장하며
     * (HTTP 경계 {@code @NotNull}), 이 태스크의 범위는 "음수·역전 금지"다.
     */
    private static void validatePrices(Integer originalPrice, Integer discountPrice) {
        if (originalPrice != null && originalPrice < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_NEGATIVE,
                ErrorCode.PRODUCT_PRICE_NEGATIVE.getDefaultMessage() + " 정가: " + originalPrice);
        }
        if (discountPrice != null && discountPrice < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_NEGATIVE,
                ErrorCode.PRODUCT_PRICE_NEGATIVE.getDefaultMessage() + " 할인가: " + discountPrice);
        }
        if (originalPrice != null && discountPrice != null && discountPrice > originalPrice) {
            throw new BusinessException(ErrorCode.PRODUCT_DISCOUNT_PRICE_EXCEEDS_ORIGINAL,
                ErrorCode.PRODUCT_DISCOUNT_PRICE_EXCEEDS_ORIGINAL.getDefaultMessage()
                    + " 정가: " + originalPrice + ", 할인가: " + discountPrice);
        }
    }

    public Integer getDiscountPrice() {
        return discountInfo != null ? discountInfo.discountPrice() : null;
    }

    public BigDecimal getDiscountRate() {
        return discountInfo != null ? discountInfo.discountRate() : null;
    }

    public void updateReviewStats(Double rating, Integer reviewCount) {
        this.rating = rating;
        this.reviewCount = reviewCount;
    }

    /**
     * 기간 없이 품절 처리한다 — {@code soldOutUntil}을 건드리지 않으므로 수동 해제까지 유지되는
     * 무기한 품절이 된다.
     *
     * <p>기존 admin 경로({@code PATCH /api/products/v1/{id}/sold-out})가 호출하고 있어
     * 무인자 시그니처를 유지한다.
     */
    public void markSoldOut() {
        this.soldOut = true;
    }

    /**
     * 자동해제 시각을 지정해 품절 처리한다. 기간 유효성(현재+30분 ~ 현재+7일)은 이 시각을 산출·검증하는
     * 도메인 서비스가 판정하므로 여기서는 값만 반영한다.
     */
    public void markSoldOut(LocalDateTime soldOutUntil) {
        this.soldOut = true;
        this.soldOutUntil = soldOutUntil;
    }

    /**
     * 품절을 해제한다. {@code soldOut}과 {@code soldOutUntil}을 <b>함께</b> 정리한다.
     *
     * <p>배치 자동해제·수동 해제·일괄 해제가 모두 이 메서드 하나만 경유해야 한다 — 한쪽이라도
     * {@code soldOut}만 끄면 {@code soldOutUntil}이 남아 다음 배치 주기가 이미 판매중인 행을 다시 집는
     * 드리프트가 생긴다.
     */
    public void releaseSoldOut() {
        this.soldOut = false;
        this.soldOutUntil = null;
    }

    /**
     * 품절 자동해제 시각만 변경한다.
     *
     * <p>품절 상태가 아니면 바꿀 대상이 없으므로 {@code PRODUCT_NOT_SOLD_OUT}(400)으로 거부한다 —
     * 판매중인 메뉴에 해제 시각만 남으면 그 값이 어떤 의미도 갖지 못한다.
     */
    public void changeSoldOutUntil(LocalDateTime until) {
        if (!this.soldOut) {
            throw new BusinessException(ErrorCode.PRODUCT_NOT_SOLD_OUT);
        }
        this.soldOutUntil = until;
    }

    public void deactivate() {
        this.visible = false;
    }

    public void activate() {
        this.visible = true;
    }

    public void update(
        ProductCategoryId productCategoryId,
        String name,
        String description,
        Integer originalPrice,
        Integer discountPrice,
        BigDecimal discountRate,
        boolean representative,
        Integer spiciness,
        boolean soldOut,
        boolean visible,
        Integer sort
    ) {
        validatePrices(originalPrice, discountPrice);

        this.productCategoryId = productCategoryId;
        this.name = name;
        this.description = description;
        this.originalPrice = originalPrice;
        this.discountInfo = ProductDiscountInfo.of(discountPrice, discountRate);
        this.representative = representative;
        this.spiciness = spiciness;
        this.soldOut = soldOut;
        this.visible = visible;
        this.sort = sort;

        // 이 경로는 soldOutUntil을 다루지 않지만, 품절이 해제되는 방향이면 함께 비워 드리프트를 남기지 않는다.
        // (판매중인데 해제 예정 시각만 남는 상태를 만들지 않는다.)
        if (!soldOut) {
            this.soldOutUntil = null;
        }
    }
}
