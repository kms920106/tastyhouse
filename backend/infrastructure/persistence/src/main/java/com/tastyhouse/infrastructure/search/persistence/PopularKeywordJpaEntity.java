package com.tastyhouse.infrastructure.search.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

@Entity
@Table(name = "POPULAR_KEYWORD",
    indexes = @Index(name = "idx_popular_keyword_active_rank", columnList = "is_visible, `rank`"))
public class PopularKeywordJpaEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "`rank`", nullable = false)
    private int rank;

    @Column(name = "is_new", nullable = false)
    private boolean newKeyword;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    protected PopularKeywordJpaEntity() {
    }

    private PopularKeywordJpaEntity(String keyword, int rank, boolean newKeyword, boolean visible) {
        this.keyword = keyword;
        this.rank = rank;
        this.newKeyword = newKeyword;
        this.visible = visible;
    }

    static PopularKeywordJpaEntity create(String keyword, int rank, boolean newKeyword, boolean visible) {
        return new PopularKeywordJpaEntity(keyword, rank, newKeyword, visible);
    }

    public Long getId() {
        return this.id;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public int getRank() {
        return this.rank;
    }

    public boolean isNewKeyword() {
        return this.newKeyword;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
