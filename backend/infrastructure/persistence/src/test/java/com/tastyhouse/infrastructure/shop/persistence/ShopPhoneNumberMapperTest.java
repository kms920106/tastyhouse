package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopPhoneNumberMapperTest {

    @Test
    @DisplayName("ShopPhoneNumber 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopPhoneNumber() {
        ShopPhoneNumber original = ShopPhoneNumber.reconstitute(
            161L,
            ShopId.of(162L),
            "v63",
            true,
            false,
            LocalDateTime.of(2026, 1, 11, 10, 6),
            LocalDateTime.of(2026, 1, 12, 10, 7)
        );

        ShopPhoneNumberJpaEntity entity = ShopPhoneNumberMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(162L);
        assertThat(entity.getPhoneNumber()).isEqualTo("v63");
        assertThat(entity.isPrimary()).isEqualTo(true);
        assertThat(entity.isVirtual()).isEqualTo(false);
    }

    @Test
    @DisplayName("ShopPhoneNumber 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopPhoneNumber() {
        ShopPhoneNumber original = ShopPhoneNumber.reconstitute(
            161L,
            ShopId.of(162L),
            "v63",
            true,
            false,
            LocalDateTime.of(2026, 1, 11, 10, 6),
            LocalDateTime.of(2026, 1, 12, 10, 7)
        );

        ShopPhoneNumberJpaEntity entity = ShopPhoneNumberJpaEntity.create(
            162L,
            "v63",
            true,
            false
        );
        ReflectionTestUtils.setField(entity, "id", 161L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 11, 10, 6));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 12, 10, 7));

        assertThat(ShopPhoneNumberMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
