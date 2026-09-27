package com.tastyhouse.application.faq.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.faq.model.Faq;
import com.tastyhouse.domain.faq.model.FaqCategory;
import com.tastyhouse.domain.faq.vo.FaqCategoryId;

import static org.assertj.core.api.Assertions.assertThat;

class FaqStateMapperTest {

    @Test
    @DisplayName("Faq → FaqState → Faq 왕복 시 모든 필드가 보존된다")
    void faqRoundTrip() {
        Faq original = Faq.reconstitute(
            31L, FaqCategoryId.of(32L), "질문", "답변", 4, true, false,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0));

        Faq restored = FaqStateMapper.toDomain(FaqStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("FaqCategory → FaqCategoryState → FaqCategory 왕복 시 모든 필드가 보존된다")
    void faqCategoryRoundTrip() {
        FaqCategory original = FaqCategory.reconstitute(
            33L, "카테고리", 2, false, true,
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0));

        FaqCategory restored = FaqCategoryStateMapper.toDomain(FaqCategoryStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
