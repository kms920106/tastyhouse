package com.tastyhouse.core.domain.notice.domain.model;

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

@Getter
@Entity
@Table(name = "NOTICE")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "title", nullable = false, length = 200)
    private String title; // 공지사항 제목

    @Column(name = "content", nullable = false, length = 1000)
    private String content; // 공지사항 본문 내용

    @Column(name = "is_visible", nullable = false)
    private boolean visible = true; // 노출 여부 (true: 노출)

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted = false; // 삭제 여부 (true: 삭제됨, Soft Delete)

    private Notice(String title, String content, boolean visible) {
        this.title = title;
        this.content = content;
        this.visible = visible;
    }

    public static Notice of(String title, String content, boolean visible) {
        return new Notice(title, content, visible);
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
