package com.tastyhouse.infrastructure.shop.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 지하철역 JPA 영속 모델. 순수 도메인 모델 {@code Station}과 분리된 영속 전용 엔티티다.
 */
@Entity
@Table(name = "STATION")
public class StationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // PK

    // 조회는 QueryDSL 투영으로만 이뤄지고 엔티티→도메인 재구성 매퍼가 없어 getter가 없다 — IDE가
    // "never used"로 경고하지만 정상이다.
    @Column(name = "station_name", nullable = false)
    private String stationName; // 지하철역 이름

    public Long getId() {
        return this.id;
    }
}
