package com.tastyhouse.infrastructure.faq.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * FAQ JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code Faq}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code FaqMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "FAQ")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FaqJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "faq_category_id", nullable = false)
    private Long faqCategoryId;

    @Column(name = "question", nullable = false, length = 500)
    private String question;

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    private FaqJpaEntity(Long faqCategoryId, String question, String answer, Integer sort, boolean visible, boolean deleted) {
        this.faqCategoryId = faqCategoryId;
        this.question = question;
        this.answer = answer;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code FaqMapper#toEntity}에서만 호출한다.
     */
    static FaqJpaEntity create(Long faqCategoryId, String question, String answer, Integer sort, boolean visible, boolean deleted) {
        return new FaqJpaEntity(faqCategoryId, question, answer, sort, visible, deleted);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(Long faqCategoryId, String question, String answer, Integer sort, boolean visible, boolean deleted) {
        this.faqCategoryId = faqCategoryId;
        this.question = question;
        this.answer = answer;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
    }
}
