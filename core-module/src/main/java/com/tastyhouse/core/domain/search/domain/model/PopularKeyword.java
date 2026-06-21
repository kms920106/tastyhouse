package com.tastyhouse.core.domain.search.domain.model;

import com.tastyhouse.core.shared.entity.BaseEntity;
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
    indexes = @Index(name = "idx_popular_keyword_active_rank", columnList = "is_visible, rank"))
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

    @Column(name = "is_visible", nullable = false)
    private Boolean isVisible;

    private PopularKeyword(String keyword, int rank, Boolean isNew, Boolean isVisible) {
        this.keyword = keyword;
        this.rank = rank;
        this.isNew = isNew;
        this.isVisible = isVisible;
    }

    public static PopularKeyword of(String keyword, int rank, boolean isNew) {
        return new PopularKeyword(keyword, rank, isNew, true);
    }
}
