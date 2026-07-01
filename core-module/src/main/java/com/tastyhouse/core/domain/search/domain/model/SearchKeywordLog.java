package com.tastyhouse.core.domain.search.domain.model;

import java.time.LocalDateTime;

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
@Table(name = "SEARCH_KEYWORD_LOG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SearchKeywordLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    private SearchKeywordLog(String keyword, LocalDateTime searchedAt) {
        this.keyword = keyword;
        this.searchedAt = searchedAt;
    }

    public static SearchKeywordLog of(String keyword) {
        return new SearchKeywordLog(keyword, LocalDateTime.now());
    }
}
