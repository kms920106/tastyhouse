package com.tastyhouse.core.domain.search.domain.model;

import com.tastyhouse.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "POPULAR_KEYWORD",
    indexes = @Index(name = "idx_popular_keyword_active_rank", columnList = "is_active, rank"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PopularKeyword extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "rank", nullable = false)
    private int rank;

    @Column(name = "is_new", nullable = false)
    private Boolean isNew;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    private PopularKeyword(String keyword, int rank, Boolean isNew, Boolean isActive) {
        this.keyword = keyword;
        this.rank = rank;
        this.isNew = isNew;
        this.isActive = isActive;
    }

    public static PopularKeyword of(String keyword, int rank, boolean isNew) {
        return new PopularKeyword(keyword, rank, isNew, true);
    }
}
