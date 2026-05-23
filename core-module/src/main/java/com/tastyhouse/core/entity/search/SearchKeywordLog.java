package com.tastyhouse.core.entity.search;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "SEARCH_KEYWORD_LOG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SearchKeywordLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "keyword", nullable = false)
    private String keyword; // 검색어

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt; // 검색 일시

    private SearchKeywordLog(String keyword, LocalDateTime searchedAt) {
        this.keyword = keyword;
        this.searchedAt = searchedAt;
    }

    public static SearchKeywordLog of(String keyword) {
        return new SearchKeywordLog(keyword, LocalDateTime.now());
    }
}
