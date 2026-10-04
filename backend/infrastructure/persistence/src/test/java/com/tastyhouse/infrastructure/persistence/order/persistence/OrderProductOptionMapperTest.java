package com.tastyhouse.infrastructure.persistence.order.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductOptionMapperTest {

    @Test
    @DisplayName("OrderProductOption → 엔티티 변환 시 모든 컬럼 값이 보존된다")
    void orderProductOptionToEntity() {
        OrderProductOption original = OrderProductOption.reconstitute(
            31L,
            OrderProductId.of(32L),
            ProductOptionGroupId.of(33L),
            "맵기",
            ProductOptionId.of(34L),
            "아주 맵게",
            500,
            "CUP",
            2,
            300
        );

        OrderProductOptionJpaEntity entity = OrderProductOptionMapper.toEntity(original);

        assertThat(entity)
            .extracting(
                "orderProductId",
                "optionGroupId",
                "optionGroupName",
                "optionId",
                "optionName",
                "additionalPrice",
                "optionGroupType",
                "cupCount",
                "depositAmount"
            )
            .containsExactly(32L, 33L, "맵기", 34L, "아주 맵게", 500, "CUP", 2, 300);
    }

    @Test
    @DisplayName("OrderProductOption의 VO가 null이면 엔티티 컬럼도 null이다")
    void orderProductOptionToEntityWithNullIds() {
        OrderProductOption original = OrderProductOption.reconstitute(
            31L, null, null, "맵기", null, "아주 맵게", 500, "CUP", 2, 300);

        OrderProductOptionJpaEntity entity = OrderProductOptionMapper.toEntity(original);

        assertThat(entity)
            .extracting("orderProductId", "optionGroupId", "optionId")
            .containsExactly(null, null, null);
    }
}
