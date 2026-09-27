package com.tastyhouse.infrastructure.shop.persistence;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.shop.port.out.write.ShopState;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ShopMapperTest {
    @Test
    @DisplayName("nullable FK가 전부 null인 엔티티를 State로 변환해도 예외가 나지 않고 FK는 null로 남는다")
    void toStateDoesNotThrowWhenNullableFksAreNull() {
        ShopJpaEntity entity = ShopJpaEntity.create(
            null,
            1L,
            "가게",
            BigDecimal.ONE,
            BigDecimal.ONE,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            false,
            false,
            0,
            false,
            false,
            false
        );

        assertThatCode(() -> ShopMapper.toState(entity)).doesNotThrowAnyException();

        ShopState state = ShopMapper.toState(entity);
        assertThat(state.ceoId()).isNull();
        assertThat(state.thumbnailImageFileId()).isNull();
        assertThat(state.trademarkImageFileId()).isNull();
    }

    @Test
    @DisplayName("nullable FK가 전부 null인 State를 엔티티로 변환해도 예외가 나지 않는다")
    void toEntityDoesNotThrowWhenNullableFksAreNull() {
        ShopState state = new ShopState(
            null,
            null,
            1L,
            "가게",
            BigDecimal.ONE,
            BigDecimal.ONE,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            false,
            false,
            0,
            false,
            false,
            false,
            null,
            null
        );

        assertThatCode(() -> ShopMapper.toEntity(state)).doesNotThrowAnyException();

        ShopJpaEntity entity = ShopMapper.toEntity(state);
        assertThat(entity.getCeoId()).isNull();
        assertThat(entity.getThumbnailImageFileId()).isNull();
        assertThat(entity.getTrademarkImageFileId()).isNull();
    }
}
