package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ShopPhoneNumber;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopPhoneNumberStateMapperTest {

    @Test
    @DisplayName("ShopPhoneNumber → ShopPhoneNumberState → ShopPhoneNumber 왕복 시 모든 필드가 보존된다")
    void shopPhoneNumberRoundTrip() {
        ShopPhoneNumber original = ShopPhoneNumber.reconstitute(
            161L,
            ShopId.of(162L),
            "v63",
            true,
            false,
            LocalDateTime.of(2026, 1, 11, 10, 6),
            LocalDateTime.of(2026, 1, 12, 10, 7)
        );

        ShopPhoneNumber restored = ShopPhoneNumberStateMapper.toDomain(ShopPhoneNumberStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
