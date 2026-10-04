package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.file.vo.UploadedFileId;
import com.tastyhouse.domain.shop.model.ShopContentBoard;
import com.tastyhouse.domain.shop.model.ShopContentTopic;
import com.tastyhouse.domain.shop.model.ShopContentType;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopContentBoardMapperTest {

    @Test
    @DisplayName("ShopContentBoard 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopContentBoard() {
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

        ShopContentBoardJpaEntity entity = ShopContentBoardMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(128L);
        assertThat(entity.getContentType()).isEqualTo("VIDEO");
        assertThat(entity.getTopic()).isEqualTo("FOOD_STORY");
        assertThat(entity.getImageFileId()).isEqualTo(131L);
        assertThat(entity.getYoutubeUrl()).isEqualTo("v32");
        assertThat(entity.getDescription()).isEqualTo("v33");
        assertThat(entity.isHidden()).isEqualTo(true);
    }

    @Test
    @DisplayName("ShopContentBoard 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopContentBoard() {
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

        ShopContentBoardJpaEntity entity = ShopContentBoardJpaEntity.create(
            128L,
            "VIDEO",
            "FOOD_STORY",
            131L,
            "v32",
            "v33",
            true
        );
        ReflectionTestUtils.setField(entity, "id", 127L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 8, 10, 35));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 9, 10, 36));

        assertThat(ShopContentBoardMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
