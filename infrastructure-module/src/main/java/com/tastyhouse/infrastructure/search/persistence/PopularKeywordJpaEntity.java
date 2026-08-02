package com.tastyhouse.infrastructure.search.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * 인기 검색어 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code PopularKeyword}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code PopularKeywordMapper}가 수행한다.
 */
@Entity
@Table(name = "POPULAR_KEYWORD",
    indexes = @Index(name = "idx_popular_keyword_active_rank", columnList = "is_visible, `rank`"))
public class PopularKeywordJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "keyword", nullable = false)
    private String keyword;

    @Column(name = "`rank`", nullable = false)
    private int rank;

    @Column(name = "is_new", nullable = false)
    private boolean newKeyword;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    protected PopularKeywordJpaEntity() {
    }

    private PopularKeywordJpaEntity(String keyword, int rank, boolean newKeyword, boolean visible) {
        this.keyword = keyword;
        this.rank = rank;
        this.newKeyword = newKeyword;
        this.visible = visible;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code PopularKeywordMapper#toEntity}에서만 호출한다.
     */
    static PopularKeywordJpaEntity create(String keyword, int rank, boolean newKeyword, boolean visible) {
        return new PopularKeywordJpaEntity(keyword, rank, newKeyword, visible);
    }

    public Long getId() {
        return this.id;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public int getRank() {
        return this.rank;
    }

    public boolean isNewKeyword() {
        return this.newKeyword;
    }

    public boolean isVisible() {
        return this.visible;
    }
}
