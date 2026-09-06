package com.tastyhouse.domain.product.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.product.vo.ProductId;
import com.tastyhouse.domain.product.vo.ProductPriceId;
import com.tastyhouse.domain.shared.model.OrderMethod;

/**
 * 메뉴 가격 한 행 — 가격명 하나에 채널별 가격 세 벌(배달·매장·픽업)을 담는 순수 도메인 모델.
 *
 * <p><b>왜 {@code PRODUCT}에 컬럼을 붙이지 않고 별도 애그리거트인가</b>: 컬럼 방식으로는 가격명
 * (보통/곱빼기)을 표현할 수 없다. 한 메뉴가 가격명을 가진 여러 가격 행을 갖는 구조가 요구사항이므로
 * 행으로 분리한다.
 *
 * <p><b>기존 {@code PRODUCT.original_price}는 지우지 않는다.</b> 주문·검색·오늘의할인·목록 등 수십 곳이
 * 그 컬럼을 읽고 있어 한 번에 걷어내면 회귀 범위가 통제 불가능하다. {@code sort=0} 행의 배달가를
 * 그 컬럼에 동기화해 유지하므로, <b>가격 행이 1개뿐인 메뉴(대부분)는 기존 동작이 완전히 그대로다</b> —
 * 이 이중화가 이 설계의 안전장치이며, 단일화는 후속 과제로 남긴다.
 *
 * <p><b>채널별 가격의 의미가 서로 다르다</b>:
 * <ul>
 *   <li>{@code deliveryPrice} — 배달·테이블·예약 주문의 <b>실제 결제 가격</b>. 상시 변경 가능</li>
 *   <li>{@code storePrice} — 오프라인 매장 가격. '매장과 같은 가격' 뱃지의 근거일 뿐
 *       <b>결제에 쓰이지 않는 표시 전용</b>이다</li>
 *   <li>{@code pickupPrice} — 포장({@code TAKEOUT}) 주문의 결제 가격. 미설정이면 배달가를 쓴다</li>
 * </ul>
 *
 * <p>{@code storePrice}·{@code pickupPrice}는 <b>매장 가격 인증 승인 후에만</b> 설정할 수 있다. 그 판정은
 * 가게 애그리거트를 함께 읽어야 하므로 이 모델이 아니라 {@code ProductPriceService}가 소유한다.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code ProductPriceJpaEntity} + {@code ProductPriceMapper}가 담당하며, 더티 체킹이 없으므로 변경 후
 * 저장은 호출부가 명시적으로 {@code ProductPriceRepository#save}를 호출해야 한다.
 */
public class ProductPrice {

    private static final int PRICE_NAME_MAX_LENGTH = 50;

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final ProductId productId;
    private String priceName; // nullable — null이면 단일 가격(가격명 없음)
    private Integer deliveryPrice;
    private Integer storePrice; // nullable — 인증 후에만 설정
    private Integer pickupPrice; // nullable — 인증 후에만 설정. 미설정이면 배달가를 쓴다
    private Integer sort;
    /**
     * 픽업가가 마지막으로 설정된 시각. '매장가격 픽업' 뱃지의 <b>익일(영업일) 노출</b> 판정 근거다.
     *
     * <p>설정 시각을 남기지 않으면 "언제부터 노출해도 되는가"를 물을 수 없어 규정을 지킬 수 없다.
     * 픽업가를 비우면 이 값도 함께 비워, 껐다 켠 픽업가가 과거 시각을 근거로 즉시 노출되는 일을 막는다.
     */
    private LocalDateTime pickupPriceSetAt;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private ProductPrice(
        Long id,
        ProductId productId,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.productId = productId;
        this.priceName = priceName;
        this.deliveryPrice = deliveryPrice;
        this.storePrice = storePrice;
        this.pickupPrice = pickupPrice;
        this.sort = sort;
        this.pickupPriceSetAt = pickupPriceSetAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * 새 가격 행을 만든다. 가격 불변식({@link #validatePrices})을 강제한다.
     *
     * @param pickupPriceSetAt 픽업가 설정 시각. {@code pickupPrice}가 있을 때만 의미가 있다
     */
    public static ProductPrice of(
        ProductId productId,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt
    ) {
        validatePriceName(priceName);
        validatePrices(deliveryPrice, storePrice, pickupPrice);

        return new ProductPrice(
            null,
            productId,
            priceName,
            deliveryPrice,
            storePrice,
            pickupPrice,
            sort,
            pickupPrice != null ? pickupPriceSetAt : null,
            null,
            null
        );
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     *
     * <p><b>{@link #of}와 달리 불변식 검증을 하지 않는다</b> — 불변식 도입 이전에 저장된 기존 행이 새
     * 규칙을 위반하더라도 로드는 가능해야 하기 때문이다({@code Product}의 같은 판단).
     */
    public static ProductPrice reconstitute(
        Long id,
        ProductId productId,
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime pickupPriceSetAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new ProductPrice(
            id,
            productId,
            priceName,
            deliveryPrice,
            storePrice,
            pickupPrice,
            sort,
            pickupPriceSetAt,
            createdAt,
            updatedAt
        );
    }

    /**
     * 주문유형에 따라 <b>실제 결제에 쓸 가격</b>을 해석한다 — 이 규칙의 단일 소유 지점이다.
     *
     * <table>
     *   <tr><th>주문유형</th><th>사용 가격</th></tr>
     *   <tr><td>{@code DELIVERY}·{@code TABLE}·{@code RESERVATION}</td><td>배달가</td></tr>
     *   <tr><td>{@code TAKEOUT}</td><td>픽업가, 미설정이면 배달가</td></tr>
     * </table>
     *
     * <p><b>클라이언트가 어느 가격을 쓸지 정하지 않는다.</b> 화면은 서버가 내려준 값을 표시만 하고,
     * 주문 금액 검증은 서버가 {@code OrderMethod}로부터 단독 결정한다 — 그러지 않으면 클라이언트가
     * 픽업가를 주장해 배달을 싸게 사는 우회가 생긴다.
     *
     * <p><b>매장가는 이 해석에 등장하지 않는다</b> — 표시 전용이므로 결제 가격이 될 수 없다.
     */
    public int resolvePrice(OrderMethod orderMethod) {
        if (orderMethod == OrderMethod.TAKEOUT && this.pickupPrice != null) {
            return this.pickupPrice;
        }
        return this.deliveryPrice;
    }

    /**
     * 이 가격 행이 미인증 상태인지와 그 사유를 판정한다. {@code null}이면 인증에 문제가 없다.
     *
     * <p>판정 순서상 <b>매장가 미등록을 먼저</b> 본다 — 매장가가 없으면 배달가와 비교할 대상 자체가
     * 없으므로 "배달가가 더 높다"고 답하면 사유가 부정확해진다.
     */
    public StorePriceUnverifiedReason resolveUnverifiedReason() {
        if (this.storePrice == null) {
            return StorePriceUnverifiedReason.STORE_PRICE_NOT_REGISTERED;
        }
        if (this.deliveryPrice != null && this.deliveryPrice > this.storePrice) {
            return StorePriceUnverifiedReason.DELIVERY_PRICE_HIGHER_THAN_STORE;
        }
        return null;
    }

    /**
     * 배달가가 매장가보다 높은지 — 재인증 필요 판정({@code 배달가 > 매장가})의 술어다.
     *
     * <p>매장가가 없으면 비교 대상이 없으므로 {@code false}다(그 경우는 "미등록"이라는 다른 사유로 잡힌다).
     */
    public boolean isDeliveryPriceHigherThanStorePrice() {
        return this.storePrice != null && this.deliveryPrice != null && this.deliveryPrice > this.storePrice;
    }

    /**
     * 매장가·픽업가가 모두 설정됐는지 — '매장가격 픽업' 뱃지의 메뉴 단위 충족 여부다.
     */
    public boolean hasStoreAndPickupPrice() {
        return this.storePrice != null && this.pickupPrice != null;
    }

    /**
     * 픽업가가 매장가 이하인지 — '매장가격 픽업' 뱃지 조건 중 하나다.
     *
     * <p>둘 중 하나라도 없으면 조건을 판정할 수 없으므로 {@code false}다.
     */
    public boolean isPickupPriceWithinStorePrice() {
        return this.storePrice != null && this.pickupPrice != null && this.pickupPrice <= this.storePrice;
    }

    /**
     * 승인된 매장가를 반영한다 — 관리자 승인 시점에만 호출한다.
     *
     * <p>{@code applyPickupSamePrice}가 켜져 있으면 픽업가도 매장가와 같게 설정한다(요구사항의
     * '픽업가격 동일 설정'). 그때 픽업가 설정 시각을 새로 남겨 뱃지의 익일 노출 기준점이 갱신된다.
     */
    public void applyVerifiedStorePrice(Integer storePrice, boolean applyPickupSamePrice, LocalDateTime now) {
        validatePrices(this.deliveryPrice, storePrice, applyPickupSamePrice ? storePrice : this.pickupPrice);

        this.storePrice = storePrice;
        if (applyPickupSamePrice) {
            this.pickupPrice = storePrice;
            this.pickupPriceSetAt = now;
        }
    }

    /**
     * 가격 행의 내용을 바꾼다(전체 교체 경로에서 기존 행을 갱신할 때).
     *
     * <p>픽업가가 <b>값이 바뀔 때만</b> 설정 시각을 갱신한다 — 같은 값을 재전송하는 전체 교체(PUT)에서
     * 시각이 매번 밀리면 뱃지가 영구히 노출되지 않는다. 픽업가를 비우면 시각도 함께 비운다.
     */
    public void change(
        String priceName,
        Integer deliveryPrice,
        Integer storePrice,
        Integer pickupPrice,
        Integer sort,
        LocalDateTime now
    ) {
        validatePriceName(priceName);
        validatePrices(deliveryPrice, storePrice, pickupPrice);

        this.priceName = priceName;
        this.deliveryPrice = deliveryPrice;
        this.storePrice = storePrice;
        this.sort = sort;

        if (pickupPrice == null) {
            this.pickupPrice = null;
            this.pickupPriceSetAt = null;
        } else {
            if (!pickupPrice.equals(this.pickupPrice)) {
                this.pickupPriceSetAt = now;
            }
            this.pickupPrice = pickupPrice;
        }
    }

    /**
     * 매장가·픽업가를 비운다 — 인증이 OFF로 내려갈 때 호출한다.
     *
     * <p>배달가는 건드리지 않는다. 인증이 풀렸다고 결제 가격이 사라져서는 안 된다.
     *
     * <p>픽업가 설정 시각도 함께 비운다 — 시각만 남으면 '매장가격 픽업' 뱃지의 익일 기준점이
     * 실효된 값을 가리킨다.
     */
    public void clearStoreAndPickupPrice() {
        this.storePrice = null;
        this.pickupPrice = null;
        this.pickupPriceSetAt = null;
    }

    /**
     * 가격명을 검증한다. {@code null}(단일 가격)은 허용하며, 길이 상한만 강제한다.
     *
     * <p>"2개 이상이면 가격명 필수·중복 불가"는 <b>행 하나로는 판정할 수 없는 컬렉션 불변식</b>이라
     * 이 모델이 아니라 {@code ProductPriceService}가 소유한다.
     */
    private static void validatePriceName(String priceName) {
        if (priceName != null && priceName.length() > PRICE_NAME_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_NAME_REQUIRED,
                "가격명은 " + PRICE_NAME_MAX_LENGTH + "자 이내여야 합니다.");
        }
    }

    /**
     * 채널별 가격 불변식을 검증한다 — 세 가격 모두 <b>0 이상</b>이어야 하고 배달가는 필수다.
     *
     * <p>기존 {@code PRODUCT_PRICE_NEGATIVE}를 재사용한다 — 같은 불변식에 코드를 둘로 두면 프론트가
     * 두 코드를 각각 분기해야 한다.
     *
     * <p><b>배달가와 매장가의 크기 관계는 여기서 막지 않는다.</b> 배달가가 매장가보다 높은 것은
     * 금지 대상이 아니라 <b>인증이 풀리는 사유</b>이므로(요구사항), 저장을 거부하지 않고
     * {@code ProductPriceService}가 인증 상태를 내린다.
     */
    private static void validatePrices(Integer deliveryPrice, Integer storePrice, Integer pickupPrice) {
        requireNonNegative(deliveryPrice, "배달 가격");
        requireNonNegative(storePrice, "매장 가격");
        requireNonNegative(pickupPrice, "픽업 가격");

        if (deliveryPrice == null) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_NEGATIVE,
                ErrorCode.PRODUCT_PRICE_NEGATIVE.getDefaultMessage() + " 배달 가격은 필수입니다.");
        }
    }

    private static void requireNonNegative(Integer price, String label) {
        if (price != null && price < 0) {
            throw new BusinessException(ErrorCode.PRODUCT_PRICE_NEGATIVE,
                ErrorCode.PRODUCT_PRICE_NEGATIVE.getDefaultMessage() + " " + label + ": " + price);
        }
    }

    public Long getId() {
        return this.id;
    }

    public ProductId getProductId() {
        return this.productId;
    }

    public String getPriceName() {
        return this.priceName;
    }

    public Integer getDeliveryPrice() {
        return this.deliveryPrice;
    }

    public Integer getStorePrice() {
        return this.storePrice;
    }

    public Integer getPickupPrice() {
        return this.pickupPrice;
    }

    public Integer getSort() {
        return this.sort;
    }

    public LocalDateTime getPickupPriceSetAt() {
        return this.pickupPriceSetAt;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }

    public ProductPriceId getProductPriceId() {
        return ProductPriceId.of(this.id);
    }
}
