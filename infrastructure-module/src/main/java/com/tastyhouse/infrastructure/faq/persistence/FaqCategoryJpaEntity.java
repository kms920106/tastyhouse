package com.tastyhouse.infrastructure.faq.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.tastyhouse.infrastructure.shared.persistence.BaseEntity;

/**
 * FAQ 카테고리 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code FaqCategory}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼/감사 필드)만
 * 담당하고 비즈니스 행위는 갖지 않는다. 도메인↔엔티티 변환은 {@code FaqCategoryMapper}가 수행한다.
 */
@Getter
@Entity
@Table(name = "FAQ_CATEGORY")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FaqCategoryJpaEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "sort", nullable = false)
    private Integer sort;

    @Column(name = "is_visible", nullable = false)
    private boolean visible;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    private FaqCategoryJpaEntity(String name, Integer sort, boolean visible, boolean deleted) {
        this.name = name;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
    }

    /**
     * 신규 저장용 엔티티를 생성한다(식별자 없음). {@code FaqCategoryMapper#toEntity}에서만 호출한다.
     */
    static FaqCategoryJpaEntity create(String name, Integer sort, boolean visible, boolean deleted) {
        return new FaqCategoryJpaEntity(name, sort, visible, deleted);
    }

    /**
     * managed 엔티티에 도메인의 변경 필드를 복사한다(update용 dirty checking 대체). 감사 필드·식별자는 건드리지 않는다.
     */
    void applyChanges(String name, Integer sort, boolean visible, boolean deleted) {
        this.name = name;
        this.sort = sort;
        this.visible = visible;
        this.deleted = deleted;
    }
}
