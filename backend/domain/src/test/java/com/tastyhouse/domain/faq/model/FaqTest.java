package com.tastyhouse.domain.faq.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.domain.faq.vo.FaqId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class FaqTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 삭제되지 않은 상태다")
    void of_createsTransientFaq() {
        Faq faq = Faq.of(FaqCategoryId.of(1L), "질문", "답변", 1, true);

        assertThat(faq.getId()).isNull();
        assertThat(faq.getFaqCategoryId()).isEqualTo(FaqCategoryId.of(1L));
        assertThat(faq.getQuestion()).isEqualTo("질문");
        assertThat(faq.getAnswer()).isEqualTo("답변");
        assertThat(faq.getSort()).isEqualTo(1);
        assertThat(faq.isVisible()).isTrue();
        assertThat(faq.isDeleted()).isFalse();
        assertThat(faq.getCreatedAt()).isNull();
        assertThat(faq.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("update는 카테고리·질문·답변·정렬·노출여부를 변경한다")
    void update_changesFields() {
        Faq faq = Faq.of(FaqCategoryId.of(1L), "질문", "답변", 1, true);

        faq.update(FaqCategoryId.of(2L), "새 질문", "새 답변", 2, false);

        assertThat(faq.getFaqCategoryId()).isEqualTo(FaqCategoryId.of(2L));
        assertThat(faq.getQuestion()).isEqualTo("새 질문");
        assertThat(faq.getAnswer()).isEqualTo("새 답변");
        assertThat(faq.getSort()).isEqualTo(2);
        assertThat(faq.isVisible()).isFalse();
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        Faq faq = Faq.of(FaqCategoryId.of(1L), "질문", "답변", 1, true);

        faq.delete();

        assertThat(faq.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        Faq faq = Faq.reconstitute(1L, FaqCategoryId.of(2L), "질문", "답변", 1, true, false, createdAt, updatedAt);

        assertThat(faq.getId()).isEqualTo(1L);
        assertThat(faq.getFaqId()).isEqualTo(FaqId.of(1L));
        assertThat(faq.getCreatedAt()).isEqualTo(createdAt);
        assertThat(faq.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("미영속 상태에서 getFaqId를 호출하면 FaqId 불변식 위반으로 예외가 발생한다")
    void getFaqId_onTransient_throws() {
        Faq faq = Faq.of(FaqCategoryId.of(1L), "질문", "답변", 1, true);

        assertThatThrownBy(faq::getFaqId)
            .isInstanceOf(IllegalArgumentException.class);
    }
}
