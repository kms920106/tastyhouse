package com.tastyhouse.infrastructure.order.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 주문 상품 옵션 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code OrderProductOption}과 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code OrderProductOptionMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "ORDER_PRODUCT_OPTION")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderProductOptionJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "order_product_id", nullable = false)
    private Long orderProductId; // 주문 상품 ID (ORDER_PRODUCT.id 참조)

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

    private OrderProductOptionJpaEntity(
        Long orderProductId,
        Long optionGroupId,
        String optionGroupName,
        Long optionId,
        String optionName,
        Integer additionalPrice
    ) {
        this.orderProductId = orderProductId;
        this.optionGroupId = optionGroupId;
        this.optionGroupName = optionGroupName;
        this.optionId = optionId;
        this.optionName = optionName;
        this.additionalPrice = additionalPrice;
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
        Integer additionalPrice
    ) {
        return new OrderProductOptionJpaEntity(
            orderProductId,
            optionGroupId,
            optionGroupName,
            optionId,
            optionName,
            additionalPrice
        );
    }
}
