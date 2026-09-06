package com.tastyhouse.infrastructure.faq.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "FAQ")
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

    protected FaqJpaEntity() {
    }

    private FaqJpaEntity(Long faqCategoryId, String question, String answer, Integer sort, boolean visible, boolean deleted) {
        this.faqCategoryId = faqCategoryId;
        this.question = question;
        this.answer = answer;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
    }

    static FaqJpaEntity create(Long faqCategoryId, String question, String answer, Integer sort, boolean visible, boolean deleted) {
        return new FaqJpaEntity(faqCategoryId, question, answer, sort, visible, deleted);
    }

    void applyChanges(Long faqCategoryId, String question, String answer, Integer sort, boolean visible, boolean deleted) {
        this.faqCategoryId = faqCategoryId;
        this.question = question;
        this.answer = answer;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
    }

    public Long getId() {
        return this.id;
    }

    public Long getFaqCategoryId() {
        return this.faqCategoryId;
    }

    public String getQuestion() {
        return this.question;
    }

    public String getAnswer() {
        return this.answer;
    }

    public Integer getSort() {
        return this.sort;
    }

    public boolean isVisible() {
        return this.visible;
    }

    public boolean isDeleted() {
        return this.deleted;
    }
}
