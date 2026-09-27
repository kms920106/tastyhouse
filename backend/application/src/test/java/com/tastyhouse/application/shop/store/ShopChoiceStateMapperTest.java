package com.tastyhouse.application.shop.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ShopChoice;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopChoiceStateMapperTest {

    @Test
    @DisplayName("ShopChoice → ShopChoiceState → ShopChoice 왕복 시 모든 필드가 보존된다")
    void shopChoiceRoundTrip() {
        ShopChoice original = ShopChoice.reconstitute(
            123L,
            ShopId.of(124L),
            "v25",
            "v26"
        );

        ShopChoice restored = ShopChoiceStateMapper.toDomain(ShopChoiceStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
