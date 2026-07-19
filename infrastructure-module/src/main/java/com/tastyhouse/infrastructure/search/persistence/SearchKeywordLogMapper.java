package com.tastyhouse.infrastructure.search.persistence;

import com.tastyhouse.core.domain.search.domain.model.SearchKeywordLog;

/**
 * 검색 키워드 로그 도메인 모델 ↔ JPA 엔티티 변환기. 도메인이 프레임워크-프리를 유지하도록 변환 책임을 infrastructure에 둔다.
 */
final class SearchKeywordLogMapper {

    private SearchKeywordLogMapper() {
    }

    /**
     * JPA 엔티티를 도메인 모델로 재구성한다(조회 경로).
     */
    static SearchKeywordLog toDomain(SearchKeywordLogJpaEntity entity) {
        return SearchKeywordLog.reconstitute(
            entity.getId(),
            entity.getKeyword(),
            entity.getSearchedAt()
        );
    }

    /**
     * 신규 도메인 모델을 저장용 JPA 엔티티로 변환한다(식별자 없는 상태).
     */
    static SearchKeywordLogJpaEntity toEntity(SearchKeywordLog domain) {
        return SearchKeywordLogJpaEntity.create(
            domain.getKeyword(),
            domain.getSearchedAt()
        );
    }
}
