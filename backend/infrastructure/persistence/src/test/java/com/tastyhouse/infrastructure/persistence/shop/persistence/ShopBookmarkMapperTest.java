package com.tastyhouse.infrastructure.persistence.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.shop.model.ShopBookmark;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

class ShopBookmarkMapperTest {

    @Test
    @DisplayName("ShopBookmark 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntity() {
        ShopBookmark original = ShopBookmark.reconstitute(
            104L,
            ShopId.of(105L),
            MemberId.of(106L)
        );

        ShopBookmarkJpaEntity entity = ShopBookmarkMapper.toEntity(original);

        assertThat(entity.getShopId()).isEqualTo(105L);
        assertThat(entity.getMemberId()).isEqualTo(106L);
    }

    @Test
    @DisplayName("ShopBookmark 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomain() {
        ShopBookmarkJpaEntity entity = ShopBookmarkJpaEntity.create(105L, 106L);
        ReflectionTestUtils.setField(entity, "id", 104L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 1, 2, 0, 0));

        ShopBookmark domain = ShopBookmarkMapper.toDomain(entity);

        assertThat(domain).usingRecursiveComparison().isEqualTo(ShopBookmark.reconstitute(
            104L,
            ShopId.of(105L),
            MemberId.of(106L)
        ));
    }
}
