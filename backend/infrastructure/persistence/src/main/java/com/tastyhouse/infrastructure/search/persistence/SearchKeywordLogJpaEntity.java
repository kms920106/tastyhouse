package com.tastyhouse.infrastructure.search.persistence;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "SEARCH_KEYWORD_LOG")
public class SearchKeywordLogJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    protected SearchKeywordLogJpaEntity() {
    }

    private SearchKeywordLogJpaEntity(String keyword, LocalDateTime searchedAt) {
        this.keyword = keyword;
        this.searchedAt = searchedAt;
    }

    static SearchKeywordLogJpaEntity create(String keyword, LocalDateTime searchedAt) {
        return new SearchKeywordLogJpaEntity(keyword, searchedAt);
    }

    public Long getId() {
        return this.id;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public LocalDateTime getSearchedAt() {
        return this.searchedAt;
    }
}
