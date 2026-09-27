package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ShopOrderNotice;
import com.tastyhouse.domain.shop.vo.ShopId;
import com.tastyhouse.domain.shop.vo.ShopOrderNoticeId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopOrderNoticeStateMapperTest {

    @Test
    @DisplayName("ShopOrderNotice → ShopOrderNoticeState → ShopOrderNotice 왕복 시 모든 필드가 보존된다")
    void shopOrderNoticeRoundTrip() {
        ShopOrderNotice original = ShopOrderNotice.reconstitute(
            ShopOrderNoticeId.of(112L),
            ShopId.of(113L),
            "v14",
            true,
            "v16",
            LocalDateTime.of(2026, 1, 18, 10, 17),
            LocalDateTime.of(2026, 1, 19, 10, 18)
        );

        ShopOrderNotice restored = ShopOrderNoticeStateMapper.toDomain(ShopOrderNoticeStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
