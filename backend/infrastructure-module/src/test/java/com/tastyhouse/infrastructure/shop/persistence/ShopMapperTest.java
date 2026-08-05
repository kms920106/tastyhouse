package com.tastyhouse.infrastructure.shop.persistence;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.StationId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * {@link ShopMapper}의 nullable FK(ceoId/thumbnailImageFileId/trademarkImageFileId) round-trip을
 * 검증한다. {@code IdMapping}을 거치지 않고 {@code CeoId.of(entity.getCeoId())}처럼 직접 호출했다면
 * 이 테스트가 {@code IllegalArgumentException}으로 실패했을 것이다.
 */
class ShopMapperTest {

    @Test
    @DisplayName("nullable FK가 전부 null인 엔티티를 도메인으로 재구성해도 예외가 나지 않는다")
    void toDomainDoesNotThrowWhenNullableFksAreNull() {
        ShopJpaEntity entity = ShopJpaEntity.create(
            null, // ceoId: 점주 미배정
            1L, // stationId: NOT NULL
            "가게",
            BigDecimal.ONE,
            BigDecimal.ONE,
            null,
            null,
            null,
            null,
            null, // thumbnailImageFileId
            null, // trademarkImageFileId
            false,
            false,
            false,
            0
        );

        assertThatCode(() -> ShopMapper.toDomain(entity)).doesNotThrowAnyException();

        Shop domain = ShopMapper.toDomain(entity);
        assertThat(domain.getCeoId()).isNull();
        assertThat(domain.getThumbnailImageFileId()).isNull();
        assertThat(domain.getTrademarkImageFileId()).isNull();
    }

    @Test
    @DisplayName("nullable VO가 전부 null인 도메인을 엔티티로 변환해도 예외가 나지 않는다")
    void toEntityDoesNotThrowWhenNullableVosAreNull() {
        Shop domain = Shop.reconstitute(
            null,
            null, // ceoId
            StationId.of(1L),
            "가게",
            BigDecimal.ONE,
            BigDecimal.ONE,
            null,
            null,
            null,
            null,
            null, // thumbnailImageFileId
            null, // trademarkImageFileId
            false,
            false,
            false,
            0,
            null,
            null
        );

        assertThatCode(() -> ShopMapper.toEntity(domain)).doesNotThrowAnyException();

        ShopJpaEntity entity = ShopMapper.toEntity(domain);
        assertThat(entity.getCeoId()).isNull();
        assertThat(entity.getThumbnailImageFileId()).isNull();
        assertThat(entity.getTrademarkImageFileId()).isNull();
    }
}
