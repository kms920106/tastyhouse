package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.Shop;
import com.tastyhouse.domain.shop.vo.StationId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class ShopMapperTest {

    @Test
    @DisplayName("Shop 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShop() {
        Shop original = Shop.reconstitute(
            127L,
            CeoId.of(128L),
            StationId.of(129L),
            "v30",
            new BigDecimal("31.125"),
            new BigDecimal("32.125"),
            33.5,
            "v34",
            "v35",
            "v36",
            UploadedFileId.of(137L),
            UploadedFileId.of(138L),
            true,
            false,
            true,
            42,
            false,
            true,
            false,
            LocalDateTime.of(2026, 1, 19, 10, 46),
            LocalDateTime.of(2026, 1, 20, 10, 47)
        );

        ShopJpaEntity entity = ShopMapper.toEntity(original);

        assertThat(entity.getCeoId()).isEqualTo(128L);
        assertThat(entity.getStationId()).isEqualTo(129L);
        assertThat(entity.getName()).isEqualTo("v30");
        assertThat(entity.getLatitude()).isEqualTo(new BigDecimal("31.125"));
        assertThat(entity.getLongitude()).isEqualTo(new BigDecimal("32.125"));
        assertThat(entity.getRating()).isEqualTo(33.5);
        assertThat(entity.getRoadAddress()).isEqualTo("v34");
        assertThat(entity.getLotAddress()).isEqualTo("v35");
        assertThat(entity.getPhoneNumber()).isEqualTo("v36");
        assertThat(entity.getThumbnailImageFileId()).isEqualTo(137L);
        assertThat(entity.getTrademarkImageFileId()).isEqualTo(138L);
        assertThat(entity.isPermanentlyClosed()).isEqualTo(true);
        assertThat(entity.isHidden()).isEqualTo(false);
        assertThat(entity.isClosedOnPublicHolidays()).isEqualTo(true);
        assertThat(entity.getMinOrderAmount()).isEqualTo(42);
        assertThat(entity.isScheduledOrderEnabled()).isEqualTo(false);
        assertThat(entity.isCupDepositEnabled()).isEqualTo(true);
        assertThat(entity.isStorePriceVerified()).isEqualTo(false);
    }

    @Test
    @DisplayName("Shop 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShop() {
        Shop original = Shop.reconstitute(
            127L,
            CeoId.of(128L),
            StationId.of(129L),
            "v30",
            new BigDecimal("31.125"),
            new BigDecimal("32.125"),
            33.5,
            "v34",
            "v35",
            "v36",
            UploadedFileId.of(137L),
            UploadedFileId.of(138L),
            true,
            false,
            true,
            42,
            false,
            true,
            false,
            LocalDateTime.of(2026, 1, 19, 10, 46),
            LocalDateTime.of(2026, 1, 20, 10, 47)
        );

        ShopJpaEntity entity = ShopJpaEntity.create(
            128L,
            129L,
            "v30",
            new BigDecimal("31.125"),
            new BigDecimal("32.125"),
            33.5,
            "v34",
            "v35",
            "v36",
            137L,
            138L,
            true,
            false,
            true,
            42,
            false,
            true,
            false
        );
        ReflectionTestUtils.setField(entity, "id", 127L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 19, 10, 46));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 20, 10, 47));

        assertThat(ShopMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("nullable FK가 전부 null인 엔티티를 도메인으로 변환해도 예외가 나지 않고 FK는 null로 남는다")
    void toDomainDoesNotThrowWhenNullableFksAreNull() {
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

        assertThatCode(() -> ShopMapper.toDomain(entity)).doesNotThrowAnyException();

        Shop shop = ShopMapper.toDomain(entity);
        assertThat(shop.getCeoId()).isNull();
        assertThat(shop.getThumbnailImageFileId()).isNull();
        assertThat(shop.getTrademarkImageFileId()).isNull();
    }

    @Test
    @DisplayName("nullable VO가 전부 null인 도메인을 엔티티로 변환해도 예외가 나지 않고 FK는 null이다")
    void toEntityDoesNotThrowWhenNullableFksAreNull() {
        Shop original = Shop.reconstitute(
            null,
            null,
            StationId.of(1L),
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

        assertThatCode(() -> ShopMapper.toEntity(original)).doesNotThrowAnyException();

        ShopJpaEntity entity = ShopMapper.toEntity(original);
        assertThat(entity.getCeoId()).isNull();
        assertThat(entity.getStationId()).isEqualTo(1L);
        assertThat(entity.getThumbnailImageFileId()).isNull();
        assertThat(entity.getTrademarkImageFileId()).isNull();
    }

    @Test
    @DisplayName("nullable VO가 전부 null인 엔티티도 도메인으로 복원된다")
    void entityToDomainWithNullableVosNull() {
        Shop original = Shop.reconstitute(
            null,
            null,
            StationId.of(1L),
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

        ShopJpaEntity entity = ShopMapper.toEntity(original);

        assertThat(ShopMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
