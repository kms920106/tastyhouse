package com.tastyhouse.domain.notice.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.notice.vo.NoticeId;

public class Notice {
    private final Long id;
    private String title;
    private String content;
    private boolean visible;
    private boolean deleted;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Notice(
        Long id,
        String title,
        String content,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.visible = visible;
        this.deleted = deleted;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Notice of(String title, String content, boolean visible) {
        return new Notice(null, title, content, visible, false, null, null);
    }

    public static Notice reconstitute(
        Long id,
        String title,
        String content,
        boolean visible,
        boolean deleted,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
        return new Notice(id, title, content, visible, deleted, createdAt, updatedAt);
    }

    public Long getId() {
        return this.id;
    }

    public String getTitle() {
        return this.title;
    }

    public String getContent() {
        return this.content;
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

    public NoticeId getNoticeId() {
        return NoticeId.of(this.id);
    }

    public void update(String title, String content, boolean visible) {
        this.title = title;
        this.content = content;
        this.visible = visible;
    }

    public void delete() {
        this.deleted = true;
    }
}
