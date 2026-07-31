package com.tastyhouse.domain.search.domain.model;

import java.time.LocalDateTime;

import lombok.Getter;

/**
 * 검색 키워드 로그 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code SearchKeywordLogJpaEntity} + {@code SearchKeywordLogMapper}가 담당한다.
 */
@Getter
public class SearchKeywordLog {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final String keyword; // 검색 키워드
    private final LocalDateTime searchedAt; // 검색 일시

    private SearchKeywordLog(Long id, String keyword, LocalDateTime searchedAt) {
        this.id = id;
        this.keyword = keyword;
        this.searchedAt = searchedAt;
    }

    /**
     * 신규 검색 키워드 로그를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static SearchKeywordLog of(String keyword) {
        return new SearchKeywordLog(null, keyword, LocalDateTime.now());
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static SearchKeywordLog reconstitute(Long id, String keyword, LocalDateTime searchedAt) {
        return new SearchKeywordLog(id, keyword, searchedAt);
    }
}
