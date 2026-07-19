package com.tastyhouse.core.domain.search.domain.model;

import lombok.Getter;

/**
 * 추천 검색어 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code RecommendedKeywordJpaEntity} + {@code RecommendedKeywordMapper}가 담당한다.
 * Java 애플리케이션 계층에 생성/변경 경로가 없는 읽기 전용 애그리거트(SQL/수동 시드)이므로
 * 신규 생성 팩토리({@code of})는 두지 않는다.
 */
@Getter
public class RecommendedKeyword {

    private final Long id;
    private final String keyword; // 검색어
    private final int sortOrder; // 정렬 순서
    private final boolean visible; // 노출 여부 (true: 노출)

    private RecommendedKeyword(Long id, String keyword, int sortOrder, boolean visible) {
        this.id = id;
        this.keyword = keyword;
        this.sortOrder = sortOrder;
        this.visible = visible;
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이며,
     * 불변식을 우회한 임의 생성을 막기 위해 이 팩토리로만 식별자를 주입한다.
     */
    public static RecommendedKeyword reconstitute(Long id, String keyword, int sortOrder, boolean visible) {
        return new RecommendedKeyword(id, keyword, sortOrder, visible);
    }
}
