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
@Table(name = "FAQ_CATEGORY")
public class FaqCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 카테고리 이름

    @Column(name = "sort", nullable = false)
    private Integer sort; // 카테고리 노출 순서

    @Column(name = "is_active", nullable = false)
    private Boolean active = true; // 활성화 여부 (true: 활성)
}
