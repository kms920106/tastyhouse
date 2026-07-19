package com.tastyhouse.core.domain.faq.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.core.domain.faq.domain.vo.FaqCategoryId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class FaqCategoryTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 삭제되지 않은 상태다")
    void of_createsTransientFaqCategory() {
        FaqCategory faqCategory = FaqCategory.of("카테고리", 1, true);

        assertThat(faqCategory.getId()).isNull();
        assertThat(faqCategory.getName()).isEqualTo("카테고리");
        assertThat(faqCategory.getSort()).isEqualTo(1);
        assertThat(faqCategory.isVisible()).isTrue();
        assertThat(faqCategory.isDeleted()).isFalse();
        assertThat(faqCategory.getCreatedAt()).isNull();
        assertThat(faqCategory.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("update는 이름·정렬·노출여부를 변경한다")
    void update_changesFields() {
        FaqCategory faqCategory = FaqCategory.of("카테고리", 1, true);

        faqCategory.update("새 카테고리", 2, false);

        assertThat(faqCategory.getName()).isEqualTo("새 카테고리");
        assertThat(faqCategory.getSort()).isEqualTo(2);
        assertThat(faqCategory.isVisible()).isFalse();
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        FaqCategory faqCategory = FaqCategory.of("카테고리", 1, true);

        faqCategory.delete();

        assertThat(faqCategory.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        FaqCategory faqCategory = FaqCategory.reconstitute(1L, "카테고리", 1, true, false, createdAt, updatedAt);

        assertThat(faqCategory.getId()).isEqualTo(1L);
        assertThat(faqCategory.getFaqCategoryId()).isEqualTo(FaqCategoryId.of(1L));
        assertThat(faqCategory.getCreatedAt()).isEqualTo(createdAt);
        assertThat(faqCategory.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("미영속 상태에서 getFaqCategoryId를 호출하면 FaqCategoryId 불변식 위반으로 예외가 발생한다")
    void getFaqCategoryId_onTransient_throws() {
        FaqCategory faqCategory = FaqCategory.of("카테고리", 1, true);

        assertThatThrownBy(faqCategory::getFaqCategoryId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
