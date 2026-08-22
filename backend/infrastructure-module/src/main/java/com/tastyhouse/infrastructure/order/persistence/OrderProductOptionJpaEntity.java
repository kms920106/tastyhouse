package com.tastyhouse.infrastructure.order.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 주문 상품 옵션 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code OrderProductOption}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code OrderProductOptionMapper}가 수행한다.
 */
@Entity
@Table(name = "ORDER_PRODUCT_OPTION")
public class OrderProductOptionJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "order_product_id", nullable = false)
    private Long orderProductId; // 주문 상품 ID (ORDER_PRODUCT.id 참조)

    // 아래 옵션 스냅샷 4필드는 insert 시에만 쓰이고 도메인으로 되읽는 경로가 없어 getter가 없다 — IDE가
    // "assigned but never accessed"로 경고하지만, JPA가 flush 시 리플렉션으로 읽는 컬럼 매핑이므로
    // 제거하면 주문 시점 옵션 스냅샷이 저장되지 않는다.

    @Column(name = "option_group_id")
    private Long optionGroupId; // 옵션 그룹 ID (스냅샷, NULL 가능)

    @Column(name = "option_group_name", nullable = false, length = 100)
    private String optionGroupName; // 주문 시점 옵션 그룹 이름 (스냅샷)

    @Column(name = "option_id")
    private Long optionId; // 옵션 ID (스냅샷, NULL 가능)

    @Column(name = "option_name", nullable = false, length = 100)
    private String optionName; // 주문 시점 옵션 이름 (스냅샷)

    @Column(name = "additional_price", nullable = false)
    private Integer additionalPrice; // 옵션 추가 금액

    /**
     * 주문 시점 옵션그룹 유형 스냅샷({@code NORMAL} / {@code CUP_DEPOSIT}).
     *
     * <p>도메인 enum이 아니라 {@code String}으로 매핑한다 — 이 값은 <b>주문 시점의 사실</b>을 박제한
     * 것이라, 나중에 유형 enum에 상수가 추가되거나 이름이 바뀌어도 과거 주문의 기록이 흔들리면 안 된다.
     * enum으로 매핑하면 알 수 없는 값에서 로드가 실패한다.
     */
    @Column(name = "option_group_type", nullable = false, length = 20)
    private String optionGroupType;

    /** 주문 시점 일회용컵 제공 개수 스냅샷. 환급 단위가 컵 개수라 금액만으로는 대체할 수 없다. */
    @Column(name = "cup_count")
    private Integer cupCount;

    /**
     * 주문 시점 보증금 금액 스냅샷(= {@code cup_count} × 당시 요율).
     * {@code additional_price}와 <b>별도 항목</b>이다 — 합치면 비과세 분리가 영구히 불가능해진다.
     */
    @Column(name = "deposit_amount", nullable = false)
    private Integer depositAmount;

    protected OrderProductOptionJpaEntity() {
    }

    private OrderProductOptionJpaEntity(
        Long orderProductId,
        Long optionGroupId,
        String optionGroupName,
        Long optionId,
        String optionName,
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        this.orderProductId = orderProductId;
        this.optionGroupId = optionGroupId;
        this.optionGroupName = optionGroupName;
        this.optionId = optionId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
        this.optionGroupType = optionGroupType;
        this.cupCount = cupCount;
        this.depositAmount = depositAmount;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code OrderProductOptionMapper#toEntity}에서만 호출한다.
     */
    static OrderProductOptionJpaEntity create(
        Long orderProductId,
        Long optionGroupId,
        String optionGroupName,
        Long optionId,
        String optionName,
        Integer additionalPrice,
        String optionGroupType,
        Integer cupCount,
        Integer depositAmount
    ) {
        return new OrderProductOptionJpaEntity(
            orderProductId,
            optionGroupId,
            optionGroupName,
            optionId,
            optionName,
            additionalPrice,
            optionGroupType,
            cupCount,
            depositAmount
        );
    }

    public Long getId() {
        return this.id;
    }

    public Long getOrderProductId() {
        return this.orderProductId;
    }

    public Integer getAdditionalPrice() {
        return this.additionalPrice;
    }

    public Integer getCupCount() {
        return this.cupCount;
    }

    public Integer getDepositAmount() {
        return this.depositAmount;
    }
}
