package com.tastyhouse.infrastructure.search.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "RECOMMENDED_KEYWORD")
public class RecommendedKeywordJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    protected RecommendedKeywordJpaEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
