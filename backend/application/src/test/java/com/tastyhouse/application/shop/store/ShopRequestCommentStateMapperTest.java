package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;

import static org.assertj.core.api.Assertions.assertThat;

class ShopRequestCommentStateMapperTest {

    @Test
    @DisplayName("ShopRequestComment → ShopRequestCommentState → ShopRequestComment 왕복 시 모든 필드가 보존된다")
    void shopRequestCommentRoundTrip() {
        ShopRequestComment original = ShopRequestComment.reconstitute(
            170L,
            171L,
            ShopRequestCommentAuthorType.CEO,
            173L,
            "v74",
            LocalDateTime.of(2026, 1, 20, 10, 15)
        );

        ShopRequestComment restored = ShopRequestCommentStateMapper.toDomain(ShopRequestCommentStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
