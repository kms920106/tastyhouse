package com.tastyhouse.core.domain.search.domain.model;

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

import com.tastyhouse.core.shared.entity.BaseEntity;

@Entity
@Table(name = "POPULAR_KEYWORD",
    indexes = @Index(name = "idx_popular_keyword_active_rank", columnList = "is_visible, `rank`"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class PopularKeyword extends BaseEntity {

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

    private PopularKeyword(String keyword, int rank, boolean newKeyword, boolean visible) {
        this.keyword = keyword;
        this.rank = rank;
        this.newKeyword = newKeyword;
        this.visible = visible;
    }

    public static PopularKeyword of(String keyword, int rank, boolean newKeyword) {
        return new PopularKeyword(keyword, rank, newKeyword, true);
    }
}
