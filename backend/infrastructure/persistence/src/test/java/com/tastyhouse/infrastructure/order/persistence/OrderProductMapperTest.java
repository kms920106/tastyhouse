package com.tastyhouse.infrastructure.order.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.order.port.out.write.OrderProductState;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductMapperTest {
    @Test
    @DisplayName("대표 이미지가 없어 imageFileId가 null인 엔티티를 State로 옮겨도 예외가 나지 않는다")
    void toStateDoesNotThrowWhenImageFileIdIsNull() {
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

        OrderProductState state = OrderProductMapper.toState(entity);

        assertThat(state.imageFileId()).isNull();
    }

    @Test
    @DisplayName("imageFileId가 null인 State를 엔티티로 변환해도 예외 없이 null이 유지된다")
    void toEntityDoesNotThrowWhenImageFileIdIsNull() {
        OrderProductState state = new OrderProductState(
            null,
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

        OrderProductJpaEntity entity = OrderProductMapper.toEntity(state);

        assertThat(entity.getImageFileId()).isNull();
    }

    @Test
    @DisplayName("imageFileId가 있으면 State↔엔티티 왕복에서 값이 보존된다")
    void imageFileIdSurvivesRoundTrip() {
        OrderProductState state = new OrderProductState(
            null,
            1L,
            2L,
            "이미지 있는 상품",
            null,
            105L,
            1,
            10000,
            null,
            0,
            10000,
            0
        );

        OrderProductJpaEntity entity = OrderProductMapper.toEntity(state);
        assertThat(entity.getImageFileId()).isEqualTo(105L);

        OrderProductState restored = OrderProductMapper.toState(entity);
        assertThat(restored.imageFileId()).isEqualTo(105L);
    }

    @Test
    @DisplayName("금액 필드는 State↔엔티티 왕복에서 서로 뒤바뀌지 않는다")
    void amountFieldsSurviveRoundTrip() {
        OrderProductState state = new OrderProductState(
            null,
            11L,
            12L,
            "상품",
            "곱빼기",
            13L,
            3,
            9000,
            8000,
            700,
            26100,
            500
        );

        OrderProductState restored = OrderProductMapper.toState(OrderProductMapper.toEntity(state));

        assertThat(restored).usingRecursiveComparison().isEqualTo(state);
    }
}
