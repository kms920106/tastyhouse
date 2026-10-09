package com.tastyhouse.infrastructure.jpa.faq.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.faq.model.FaqCategory;

import static org.assertj.core.api.Assertions.assertThat;

class FaqCategoryMapperTest {

    @Test
    @DisplayName("FaqCategory → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        FaqCategory faqCategory = FaqCategory.reconstitute(
            33L, "카테고리", 2, false, true,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0));

        FaqCategoryJpaEntity entity = FaqCategoryMapper.toEntity(faqCategory);

        assertThat(entity.getName()).isEqualTo("카테고리");
        assertThat(entity.getSort()).isEqualTo(2);
        assertThat(entity.isVisible()).isFalse();
        assertThat(entity.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("엔티티 → FaqCategory 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        FaqCategoryJpaEntity entity = FaqCategoryJpaEntity.create("카테고리", 2, false, true);
        ReflectionTestUtils.setField(entity, "id", 33L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 3, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 4, 1, 0, 0));

        FaqCategory faqCategory = FaqCategoryMapper.toDomain(entity);

        FaqCategory expected = FaqCategory.reconstitute(
            33L, "카테고리", 2, false, true,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0));
        assertThat(faqCategory).usingRecursiveComparison().isEqualTo(expected);
    }
}
