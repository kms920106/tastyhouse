package com.tastyhouse.infrastructure.faq.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;

import static org.assertj.core.api.Assertions.assertThat;

class FaqMapperTest {

    @Test
    @DisplayName("Faq → 엔티티 변환 시 모든 컬럼 값이 채워진다")
    void toEntity() {
        Faq faq = Faq.reconstitute(
            31L, FaqCategoryId.of(32L), "질문", "답변", 4, true, false,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0));

        FaqJpaEntity entity = FaqMapper.toEntity(faq);

        assertThat(entity.getFaqCategoryId()).isEqualTo(32L);
        assertThat(entity.getQuestion()).isEqualTo("질문");
        assertThat(entity.getAnswer()).isEqualTo("답변");
        assertThat(entity.getSort()).isEqualTo(4);
        assertThat(entity.isVisible()).isTrue();
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("엔티티 → Faq 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        FaqJpaEntity entity = FaqJpaEntity.create(32L, "질문", "답변", 4, true, false);
        ReflectionTestUtils.setField(entity, "id", 31L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 2, 1, 0, 0));

        Faq faq = FaqMapper.toDomain(entity);

        Faq expected = Faq.reconstitute(
            31L, FaqCategoryId.of(32L), "질문", "답변", 4, true, false,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0));
        assertThat(faq).usingRecursiveComparison().isEqualTo(expected);
    }
}
