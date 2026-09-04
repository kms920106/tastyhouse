package com.tastyhouse.application.shared.port.out;

import java.util.List;

import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;

/**
 * 저장된 도형 문자열을 도메인 기하 타입으로 푸는 읽기 포트.
 *
 * <p>좌표 인코딩 형식은 영속 계층의 지식이므로 그 해독은 infrastructure-module의
 * {@code GeoRingsResolver}가 구현한다. 소비 모듈(ceo-api)은 이 포트만 주입해 도메인 기하 타입을 받고,
 * 저장 형식을 알지 않는다 — 형식이 바뀌어도 어댑터만 고치면 된다.
 *
 * <p>{@code *QueryPort}(DAO 대응)와 달리 이 포트에는 조회가 아니라 <b>변환</b>만 있다. 그래서 이름에
 * {@code Query}를 붙이지 않고, 컨텍스트 중립이라 {@code shared} 아래에 둔다.
 */
public interface GeoRingsQueryPort {

    /**
     * 인코딩된 문자열을 링 목록으로 푼다. 값이 없으면(경계 미보유) 빈 목록 — 미보유는 정상 상태다.
     */
    List<GeoRing> resolveRings(String encoded);

    /**
     * 인코딩된 문자열을 도형으로 푼다. 값이 없으면 {@code null} — 도형 미설정도 정상 상태이므로 예외로
     * 다루지 않는다.
     */
    GeoPolygon resolvePolygon(String encoded);
}
