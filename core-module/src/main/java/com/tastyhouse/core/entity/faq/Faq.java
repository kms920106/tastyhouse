package com.tastyhouse.core.entity.faq;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "FAQ")
public class Faq extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "faq_category_id", nullable = false)
    private Long faqCategoryId; // FAQ 카테고리 ID (FAQ_CATEGORY.id 참조)

    @Column(name = "question", nullable = false, length = 500)
    private String question; // 질문 내용

    @Column(name = "answer", nullable = false, columnDefinition = "TEXT")
    private String answer; // 답변 내용

    @Column(name = "sort", nullable = false)
    private Integer sort; // FAQ 노출 순서

    @Column(name = "is_active", nullable = false)
    private Boolean active = true; // 활성화 여부 (true: 활성)
}
