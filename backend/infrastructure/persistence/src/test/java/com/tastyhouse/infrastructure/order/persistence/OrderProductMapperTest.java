package com.tastyhouse.infrastructure.order.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductMapperTest {

    @Test
    @DisplayName("대표 이미지가 없어 imageFileId가 null인 엔티티를 도메인으로 옮겨도 예외가 나지 않는다")
    void toDomainDoesNotThrowWhenImageFileIdIsNull() {
        OrderProductJpaEntity entity = OrderProductJpaEntity.create(
            1L,
            2L,
            "이미지 없는 상품",
            null,
            null,
            1,
            10000,
            null,
            0,
            10000,
            0
        );

        OrderProduct orderProduct = OrderProductMapper.toDomain(entity);

        assertThat(orderProduct.getImageFileId()).isNull();
    }

    @Test
    @DisplayName("imageFileId가 null인 도메인을 엔티티로 변환해도 예외 없이 null이 유지된다")
    void toEntityDoesNotThrowWhenImageFileIdIsNull() {
        OrderProduct orderProduct = OrderProduct.reconstitute(
            21L, OrderId.of(22L), ProductId.of(23L), "이미지 없는 상품", null, null, 1, 10000, null, 0, 10000, 0);

        OrderProductJpaEntity entity = OrderProductMapper.toEntity(orderProduct);

        assertThat(entity.getImageFileId()).isNull();
        assertThat(entity.getPriceName()).isNull();
        assertThat(entity.getDiscountPrice()).isNull();
    }

    @Test
    @DisplayName("imageFileId가 null인 엔티티를 도메인으로 옮기면 id를 포함해 원본과 같다")
    void nullImageFileIdToDomain() {
        OrderProduct original = OrderProduct.reconstitute(
            21L, OrderId.of(22L), ProductId.of(23L), "이미지 없는 상품", null, null, 1, 10000, null, 0, 10000, 0);
        OrderProductJpaEntity entity = OrderProductMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 21L);

        OrderProduct restored = OrderProductMapper.toDomain(entity);

        assertThat(restored.getImageFileId()).isNull();
        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("OrderProduct → 엔티티 변환 시 금액 필드가 서로 뒤바뀌지 않는다")
    void orderProductToEntity() {
        OrderProductJpaEntity entity = OrderProductMapper.toEntity(orderProduct());

        assertThat(entity.getOrderId()).isEqualTo(22L);
        assertThat(entity.getProductId()).isEqualTo(23L);
        assertThat(entity.getName()).isEqualTo("김치찌개");
        assertThat(entity.getPriceName()).isEqualTo("곱빼기");
        assertThat(entity.getImageFileId()).isEqualTo(24L);
        assertThat(entity.getQuantity()).isEqualTo(3);
        assertThat(entity.getOriginalPrice()).isEqualTo(9000);
        assertThat(entity.getDiscountPrice()).isEqualTo(8000);
        assertThat(entity.getTotalOptionPrice()).isEqualTo(700);
        assertThat(entity.getTotalPrice()).isEqualTo(26100);
        assertThat(entity.getCupDepositAmount()).isEqualTo(500);
    }

    @Test
    @DisplayName("엔티티 → OrderProduct 변환 시 id를 포함한 모든 필드가 보존된다")
    void orderProductToDomain() {
        OrderProduct original = orderProduct();
        OrderProductJpaEntity entity = OrderProductMapper.toEntity(original);
        ReflectionTestUtils.setField(entity, "id", 21L);

        OrderProduct restored = OrderProductMapper.toDomain(entity);

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    private static OrderProduct orderProduct() {
        return OrderProduct.reconstitute(
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
    }
}
