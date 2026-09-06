package com.tastyhouse.domain.ceo.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.ceo.vo.CeoId;

public class CeoReplyPhrase {
    private final Long id;
    private final CeoId ceoId;
    private String name;
    private String content;
    private final int sort;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private CeoReplyPhrase(
        Long id,
        CeoId ceoId,
        String name,
        String content,
        int sort,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.ceoId = ceoId;
        this.name = name;
        this.content = content;
        this.sort = sort;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static CeoReplyPhrase of(CeoId ceoId, String name, String content, int sort) {
        return new CeoReplyPhrase(null, ceoId, name, content, sort, null, null);
    }

    public static CeoReplyPhrase reconstitute(
        Long id,
        CeoId ceoId,
        String name,
        String content,
        int sort,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new CeoReplyPhrase(id, ceoId, name, content, sort, createdAt, updatedAt);
    }

    public void updateContent(String name, String content) {
        this.name = name;
        this.content = content;
    }

    public Long getId() {
        return this.id;
    }

    public CeoId getCeoId() {
        return this.ceoId;
    }

    public String getName() {
        return this.name;
    }

    public String getContent() {
        return this.content;
    }

    public int getSort() {
        return this.sort;
    }

    public LocalDateTime getCreatedAt() {
        return this.createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return this.updatedAt;
    }
}
