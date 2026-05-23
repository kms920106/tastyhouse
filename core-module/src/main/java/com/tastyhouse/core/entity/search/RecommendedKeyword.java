package com.tastyhouse.core.entity.search;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "RECOMMENDED_KEYWORD")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class RecommendedKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "keyword", nullable = false)
    private String keyword; // 추천 검색어

    @Column(name = "sort_order", nullable = false)
    private int sortOrder; // 추천 검색어 노출 순서

    @Column(name = "is_active", nullable = false)
    private Boolean isActive; // 활성화 여부 (true: 활성)
}
