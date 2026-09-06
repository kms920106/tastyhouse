package com.tastyhouse.infrastructure.order.persistence;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.order.model.OrderProduct;
import com.tastyhouse.domain.order.vo.OrderId;
import com.tastyhouse.domain.product.vo.ProductId;

import static org.assertj.core.api.Assertions.assertThat;

class OrderProductMapperTest {
    @Test
    @DisplayName("대표 이미지가 없어 imageFileId가 null인 엔티티를 도메인으로 재구성해도 예외가 나지 않는다")
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

        OrderProduct domain = OrderProductMapper.toDomain(entity);

        assertThat(domain.getImageFileId()).isNull();
    }

    @Test
    @DisplayName("imageFileId가 null인 도메인 모델을 엔티티로 변환해도 예외 없이 null이 유지된다")
    void toEntityDoesNotThrowWhenImageFileIdIsNull() {
        OrderProduct domain = OrderProduct.of(
            OrderId.of(1L),
            ProductId.of(2L),
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

        OrderProductJpaEntity entity = OrderProductMapper.toEntity(domain);

        assertThat(entity.getImageFileId()).isNull();
    }

    @Test
    @DisplayName("imageFileId가 있으면 도메인↔엔티티 왕복에서 값이 보존된다")
    void imageFileIdSurvivesRoundTrip() {
        OrderProduct domain = OrderProduct.of(
            OrderId.of(1L),
            ProductId.of(2L),
            "이미지 있는 상품",
            null,
            UploadedFileId.of(105L),
            1,
            10000,
            null,
            0,
            10000,
            0
        );

        OrderProductJpaEntity entity = OrderProductMapper.toEntity(domain);
        assertThat(entity.getImageFileId()).isEqualTo(105L);

        OrderProduct restored = OrderProductMapper.toDomain(entity);
        assertThat(restored.getImageFileId()).isEqualTo(UploadedFileId.of(105L));
    }
}
