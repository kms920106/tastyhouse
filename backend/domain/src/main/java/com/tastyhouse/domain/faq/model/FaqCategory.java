package com.tastyhouse.domain.faq.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.faq.vo.FaqCategoryId;

public class FaqCategory {
    private final Long id;
    private String name;
    private Integer sort;
    private boolean visible;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private FaqCategory(
        Long id,
        String name,
        Integer sort,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.name = name;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static FaqCategory of(String name, Integer sort, boolean visible) {
        return new FaqCategory(null, name, sort, visible, false, null, null);
    }

    public static FaqCategory reconstitute(
        Long id,
        String name,
        Integer sort,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new FaqCategory(id, name, sort, visible, deleted, createdAt, updatedAt);
    }

    public FaqCategoryId getFaqCategoryId() {
        return FaqCategoryId.of(this.id);
    }

    public void update(String name, Integer sort, boolean visible) {
        this.name = name;
        this.sort = sort;
        this.visible = visible;
    }

    public void delete() {
        this.deleted = true;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
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
