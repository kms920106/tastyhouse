package com.tastyhouse.infrastructure.notice.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "NOTICE")
public class NoticeJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", nullable = false, length = 1000)
    private String content;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    protected NoticeJpaEntity() {
    }

    private NoticeJpaEntity(String title, String content, boolean visible, boolean deleted) {
        this.title = title;
        this.content = content;
        this.visible = visible;
        this.deleted = deleted;
    }

    static NoticeJpaEntity create(String title, String content, boolean visible, boolean deleted) {
        return new NoticeJpaEntity(title, content, visible, deleted);
    }

    void applyChanges(String title, String content, boolean visible, boolean deleted) {
        this.title = title;
        this.content = content;
        this.visible = visible;
        this.deleted = deleted;
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
}
