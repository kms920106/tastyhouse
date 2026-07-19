package com.tastyhouse.core.domain.search.domain.model;

import lombok.Getter;

/**
 * 인기 검색어 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code PopularKeywordJpaEntity} + {@code PopularKeywordMapper}가 담당한다.
 */
@Getter
public class PopularKeyword {

    private final Long id; // null이면 아직 영속되지 않은 신규 상태
    private final String keyword; // 검색어
    private final int rank; // 순위
    private final boolean newKeyword; // 신규 진입 여부 (true: 신규)
    private final boolean visible; // 노출 여부 (true: 노출)

    private PopularKeyword(Long id, String keyword, int rank, boolean newKeyword, boolean visible) {
        this.id = id;
        this.keyword = keyword;
        this.rank = rank;
        this.newKeyword = newKeyword;
        this.visible = visible;
    }

    /**
     * 신규 인기 검색어를 생성한다. 아직 영속되지 않았으므로 식별자는 없다.
     */
    public static PopularKeyword of(String keyword, int rank, boolean newKeyword) {
        return new PopularKeyword(null, keyword, rank, newKeyword, true);
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static PopularKeyword reconstitute(Long id, String keyword, int rank, boolean newKeyword, boolean visible) {
        return new PopularKeyword(id, keyword, rank, newKeyword, visible);
    }
}
