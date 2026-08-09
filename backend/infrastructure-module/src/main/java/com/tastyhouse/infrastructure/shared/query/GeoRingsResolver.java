package com.tastyhouse.infrastructure.shared.query;

import java.util.List;

import org.springframework.stereotype.Component;

import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.infrastructure.shared.persistence.GeoPolygonTextCodec;

/**
 * 저장된 도형 문자열을 도메인 기하 타입으로 푸는 read 측 변환기.
 *
 * <p><b>왜 별도 빈인가</b>: 좌표 인코딩 형식은 영속 계층의 지식이라 {@code GeoPolygonTextCodec}이
 * {@code ..persistence..}에 있는데, api 모듈은 그 패키지에 의존할 수 없다(ArchUnit
 * {@code shouldNotDependOnInfrastructurePersistence}). 그렇다고 api가 인코딩 형식을 알게 하면 저장 형식이
 * 바뀔 때 api까지 함께 고쳐야 한다.
 *
 * <p>그래서 파일 경로를 표시용 URL로 완성하는 {@code FileUrlResolver}와 같은 형태를 취한다 — <b>read 측이
 * 소비자가 바로 쓸 수 있는 형태까지 완성해서 내려보낸다.</b> api는 도메인 기하 타입만 받고 저장 형식을
 * 알지 않는다.
 */
@Component
public class GeoRingsResolver {

    /**
     * 인코딩된 문자열을 링 목록으로 푼다. 값이 없으면(경계 미보유) 빈 목록 — 미보유는 정상 상태다.
     */
    public List<GeoRing> resolveRings(String encoded) {
        return GeoPolygonTextCodec.decodeRings(encoded);
    }

    /**
     * 인코딩된 문자열을 도형으로 푼다. 값이 없으면 {@code null} — 도형 미설정도 정상 상태이므로 예외로
     * 다루지 않는다.
     */
    public GeoPolygon resolvePolygon(String encoded) {
        List<GeoRing> rings = resolveRings(encoded);
        return rings.isEmpty() ? null : GeoPolygon.of(rings);
    }
}
