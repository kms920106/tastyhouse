package com.tastyhouse.infrastructure.search.persistence;

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

/**
 * 검색 키워드 로그 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code SearchKeywordLog}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code SearchKeywordLogMapper}가 수행한다.
 */
@Entity
@Table(name = "SEARCH_KEYWORD_LOG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class SearchKeywordLogJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "searched_at", nullable = false)
    private LocalDateTime searchedAt;

    private SearchKeywordLogJpaEntity(String keyword, LocalDateTime searchedAt) {
        this.keyword = keyword;
        this.searchedAt = searchedAt;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code SearchKeywordLogMapper#toEntity}에서만 호출한다.
     */
    static SearchKeywordLogJpaEntity create(String keyword, LocalDateTime searchedAt) {
        return new SearchKeywordLogJpaEntity(keyword, searchedAt);
    }
}
