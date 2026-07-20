package com.tastyhouse.core.domain.shop.domain.model;

import lombok.Getter;

/**
 * 지하철역 순수 도메인 모델.
 *
 * <p>JPA/프레임워크에 의존하지 않는 POJO다. 영속화는 infrastructure-module의
 * {@code StationJpaEntity} + {@code StationMapper}가 담당한다. 애플리케이션 계층에서
 * 신규 생성 경로가 없어(시드 데이터 전용) {@code of}는 두지 않고 {@code reconstitute}만 공개한다.
 */
@Getter
public class Station {

    private final Long id;
    private final String stationName;

    private Station(Long id, String stationName) {
        this.id = id;
        this.stationName = stationName;
    }

    /**
     * DB에 저장된 상태로부터 도메인 객체를 재구성한다. 영속 계층(infrastructure) 전용이다.
     */
    public static Station reconstitute(Long id, String stationName) {
        return new Station(id, stationName);
    }
}
