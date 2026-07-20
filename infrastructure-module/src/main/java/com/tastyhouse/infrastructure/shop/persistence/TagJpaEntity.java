package com.tastyhouse.infrastructure.shop.persistence;

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
 * 태그 JPA 영속 모델. 순수 도메인 모델 {@code Tag}와 분리된 영속 전용 엔티티다.
 */
@Getter
@Entity
@Table(name = "TAG")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TagJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    @Column(name = "tag_name", nullable = false)
    private String tagName; // 태그명

    private TagJpaEntity(String tagName) {
        this.tagName = tagName;
    }

    static TagJpaEntity create(String tagName) {
        return new TagJpaEntity(tagName);
    }
}
