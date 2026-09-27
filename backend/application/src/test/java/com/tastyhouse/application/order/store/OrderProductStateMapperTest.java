package com.tastyhouse.application.order.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductStateMapperTest {

    @Test
    @DisplayName("OrderProduct → OrderProductState → OrderProduct 왕복 시 모든 필드가 보존된다")
    void orderProductRoundTrip() {
        OrderProduct original = OrderProduct.reconstitute(
            21L,
            OrderId.of(22L),
            ProductId.of(23L),
            "김치찌개",
            "곱빼기",
            UploadedFileId.of(24L),
            3,
            9000,
            8000,
            700,
            26100,
            500
        );

        OrderProduct restored = OrderProductStateMapper.toDomain(OrderProductStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("imageFileId가 null이어도 왕복에서 null이 유지된다")
    void nullImageFileIdRoundTrip() {
        OrderProduct original = OrderProduct.reconstitute(
            21L, OrderId.of(22L), ProductId.of(23L), "이미지 없는 상품", null, null, 1, 10000, null, 0, 10000, 0);

        OrderProduct restored = OrderProductStateMapper.toDomain(OrderProductStateMapper.toState(original));

        assertThat(restored.getImageFileId()).isNull();
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
