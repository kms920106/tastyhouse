package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ShopNotice;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopNoticeStateMapperTest {

    @Test
    @DisplayName("ShopNotice → ShopNoticeState → ShopNotice 왕복 시 모든 필드가 보존된다")
    void shopNoticeRoundTrip() {
        ShopNotice original = ShopNotice.reconstitute(
            101L,
            ShopId.of(102L),
            "v3",
            true,
            false,
            LocalDateTime.of(2026, 1, 7, 10, 6),
            LocalDateTime.of(2026, 1, 8, 10, 7)
        );

        ShopNotice restored = ShopNoticeStateMapper.toDomain(ShopNoticeStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
