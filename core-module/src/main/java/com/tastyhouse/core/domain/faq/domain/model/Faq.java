package com.tastyhouse.core.domain.faq.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.domain.faq.domain.vo.FaqId;
import com.tastyhouse.core.shared.entity.BaseEntity;

@Getter
@Entity
@Table(name = "FAQ")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Faq extends BaseEntity {

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
    private boolean visible = true;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false;

    private Faq(Long faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        this.faqCategoryId = faqCategoryId;
        this.question = question;
        this.answer = answer;
        this.sort = sort;
        this.visible = visible;
    }

    public static Faq of(Long faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        return new Faq(faqCategoryId, question, answer, sort, visible);
    }

    public FaqId getFaqId() {
        return FaqId.of(this.id);
    }

    public void update(Long faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        this.faqCategoryId = faqCategoryId;
        this.question = question;
        this.answer = answer;
        this.sort = sort;
        this.visible = visible;
    }

    public void delete() {
        this.deleted = true;
    }
}
