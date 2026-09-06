package com.tastyhouse.domain.search.model;

import java.time.LocalDateTime;

public class SearchKeywordLog {
    private final Long id;
    private final String keyword;
    private final LocalDateTime searchedAt;

    private SearchKeywordLog(Long id, String keyword, LocalDateTime searchedAt) {
        this.id = id;
        this.keyword = keyword;
        this.searchedAt = searchedAt;
    }

    public static SearchKeywordLog of(String keyword) {
        return new SearchKeywordLog(null, keyword, LocalDateTime.now());
    }

    public static SearchKeywordLog reconstitute(Long id, String keyword, LocalDateTime searchedAt) {
        return new SearchKeywordLog(id, keyword, searchedAt);
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
