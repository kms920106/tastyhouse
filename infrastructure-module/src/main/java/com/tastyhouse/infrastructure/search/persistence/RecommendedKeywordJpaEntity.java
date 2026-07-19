package com.tastyhouse.infrastructure.search.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.core.shared.entity.BaseEntity;

/**
 * 추천 검색어 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code RecommendedKeyword}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. Java 애플리케이션 계층에 생성/변경 경로가 없는 읽기 전용 애그리거트이므로
 * {@code create}/{@code applyChanges}는 두지 않는다. 도메인↔엔티티 변환은 {@code RecommendedKeywordMapper}가 수행한다.
 */
@Entity
@Table(name = "RECOMMENDED_KEYWORD")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
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
}
