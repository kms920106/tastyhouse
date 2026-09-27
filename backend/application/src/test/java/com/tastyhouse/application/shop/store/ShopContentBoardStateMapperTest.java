package com.tastyhouse.application.shop.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.model.ShopContentTopic;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopContentBoardStateMapperTest {

    @Test
    @DisplayName("ShopContentBoard → ShopContentBoardState → ShopContentBoard 왕복 시 모든 필드가 보존된다")
    void shopContentBoardRoundTrip() {
        ShopContentBoard original = ShopContentBoard.reconstitute(
            127L,
            ShopId.of(128L),
            ShopContentType.VIDEO,
            ShopContentTopic.FOOD_STORY,
            UploadedFileId.of(131L),
            "v32",
            "v33",
            true,
            LocalDateTime.of(2026, 1, 8, 10, 35),
            LocalDateTime.of(2026, 1, 9, 10, 36)
        );

        ShopContentBoard restored = ShopContentBoardStateMapper.toDomain(ShopContentBoardStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
