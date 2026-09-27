package com.tastyhouse.infrastructure.shop.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.shop.model.ShopRequestComment;
import com.tastyhouse.domain.shop.model.ShopRequestCommentAuthorType;

import static org.assertj.core.api.Assertions.assertThat;

class ShopRequestCommentMapperTest {

    @Test
    @DisplayName("ShopRequestComment 도메인 → 엔티티 변환 시 컬럼 값이 보존된다")
    void domainToEntityShopRequestComment() {
        ShopRequestComment original = ShopRequestComment.reconstitute(
            170L,
            171L,
            ShopRequestCommentAuthorType.CEO,
            173L,
            "v74",
            LocalDateTime.of(2026, 1, 20, 10, 15)
        );

        ShopRequestCommentJpaEntity entity = ShopRequestCommentMapper.toEntity(original);

        assertThat(entity.getShopRequestIndexId()).isEqualTo(171L);
        assertThat(entity.getAuthorType()).isEqualTo("CEO");
        assertThat(entity.getAuthorId()).isEqualTo(173L);
        assertThat(entity.getContent()).isEqualTo("v74");
    }

    @Test
    @DisplayName("ShopRequestComment 엔티티 → 도메인 변환 시 모든 필드가 보존된다")
    void entityToDomainShopRequestComment() {
        ShopRequestComment original = ShopRequestComment.reconstitute(
            170L,
            171L,
            ShopRequestCommentAuthorType.CEO,
            173L,
            "v74",
            LocalDateTime.of(2026, 1, 20, 10, 15)
        );

        ShopRequestCommentJpaEntity entity = ShopRequestCommentJpaEntity.create(
            171L,
            "CEO",
            173L,
            "v74"
        );
        ReflectionTestUtils.setField(entity, "id", 170L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 20, 10, 15));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        assertThat(ShopRequestCommentMapper.toDomain(entity)).usingRecursiveComparison().isEqualTo(original);
    }
}
