package com.tastyhouse.infrastructure.search.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 추천 검색어 JPA 영속 모델.
 *
 * <p>DB 매핑(테이블/컬럼/감사 필드)만 담당하고 비즈니스 행위는 갖지 않는다. Java 애플리케이션 계층에
 * 생성/변경 경로가 없는 읽기 전용 애그리거트이므로 {@code create}/{@code applyChanges}는 두지 않는다.
 *
 * <p>조회 경로가 CQRS query 측({@code search/query/SearchQueryDao})으로 이관되어 이 엔티티에서 Result DTO로
 * 직접 투영하므로, 대응하는 순수 도메인 모델·write 포트·매퍼는 두지 않는다(전부 미사용이 되어 제거됨).
 */
@Entity
@Table(name = "RECOMMENDED_KEYWORD")
public class RecommendedKeywordJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    protected RecommendedKeywordJpaEntity() {
    }

    public Long getId() {
        return this.id;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
