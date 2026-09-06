package com.tastyhouse.domain.faq.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.faq.vo.FaqCategoryId;
import com.tastyhouse.domain.faq.vo.FaqId;

public class Faq {
    private final Long id;
    private FaqCategoryId faqCategoryId;
    private String question;
    private String answer;
    private Integer sort;
    private boolean visible;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Faq(
        Long id,
        FaqCategoryId faqCategoryId,
        String question,
        String answer,
        Integer sort,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.faqCategoryId = faqCategoryId;
        this.question = question;
        this.answer = answer;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Faq of(FaqCategoryId faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        return new Faq(null, faqCategoryId, question, answer, sort, visible, false, null, null);
    }

    public static Faq reconstitute(
        Long id,
        FaqCategoryId faqCategoryId,
        String question,
        String answer,
        Integer sort,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Faq(id, faqCategoryId, question, answer, sort, visible, deleted, createdAt, updatedAt);
    }

    public FaqId getFaqId() {
        return FaqId.of(this.id);
    }

    public void update(FaqCategoryId faqCategoryId, String question, String answer, Integer sort, boolean visible) {
        this.faqCategoryId = faqCategoryId;
        this.question = question;
        this.answer = answer;
        this.sort = sort;
        this.visible = visible;
    }

    public void delete() {
        this.deleted = true;
    }

    public Long getId() {
        return this.id;
    }

    public FaqCategoryId getFaqCategoryId() {
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

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
