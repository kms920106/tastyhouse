package com.tastyhouse.core.domain.notice.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "NOTICE")
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
}
