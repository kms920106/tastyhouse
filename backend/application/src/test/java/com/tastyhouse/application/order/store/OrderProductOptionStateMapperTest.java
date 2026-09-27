package com.tastyhouse.application.order.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.order.model.OrderProductOption;
import com.tastyhouse.domain.order.vo.OrderProductId;
import com.tastyhouse.domain.product.vo.ProductOptionGroupId;
import com.tastyhouse.domain.product.vo.ProductOptionId;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductOptionStateMapperTest {

    @Test
    @DisplayName("OrderProductOption → OrderProductOptionState → OrderProductOption 왕복 시 모든 필드가 보존된다")
    void orderProductOptionRoundTrip() {
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

        OrderProductOption restored =
            OrderProductOptionStateMapper.toDomain(OrderProductOptionStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
