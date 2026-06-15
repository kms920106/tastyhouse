package com.tastyhouse.core.domain.notice.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(name = "is_active", nullable = false)
    private Boolean active = true; // 활성화 여부 (true: 활성)

    private Notice(String title, String content, Boolean active) {
        this.title = title;
        this.content = content;
        this.active = active;
    }

    public static Notice of(String title, String content) {
        return new Notice(title, content, true);
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void deactivate() {
        this.active = false;
    }
}
