package com.tastyhouse.application.shop.store;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopBookmarkStateMapperTest {

    @Test
    @DisplayName("ShopBookmark → ShopBookmarkState → ShopBookmark 왕복 시 모든 필드가 보존된다")
    void shopBookmarkRoundTrip() {
        ShopBookmark original = ShopBookmark.reconstitute(
            104L,
            ShopId.of(105L),
            MemberId.of(106L)
        );

        ShopBookmark restored = ShopBookmarkStateMapper.toDomain(ShopBookmarkStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
