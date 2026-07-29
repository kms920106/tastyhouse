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
 * 금칙어 JPA 영속 모델.
 *
 * <p>순수 도메인 모델 {@code ProhibitedWord}와 분리된 영속 전용 엔티티다. DB 매핑(테이블/컬럼)만
 * 담당하고 비즈니스 행위는 갖지 않는다. Java 애플리케이션 계층에 생성/변경 경로가 없는 읽기 전용 애그리거트이므로
 * {@code create}/{@code applyChanges}는 두지 않으며, 감사 필드를 소비하는 곳이 없어 {@code BaseEntity}도
 * 상속하지 않는다(search 도메인 {@code RecommendedKeywordJpaEntity}와 달리 audit 필드 미사용). 도메인↔엔티티 변환은
 * {@code ProhibitedWordMapper}가 수행한다.
 */
@Entity
@Table(name = "PROHIBITED_WORD")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ProhibitedWordJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "word", nullable = false)
    private String word;

    @Column(name = "reason")
    private String reason;
}
