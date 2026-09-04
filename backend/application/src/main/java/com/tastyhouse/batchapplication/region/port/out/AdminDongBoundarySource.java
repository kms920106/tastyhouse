package com.tastyhouse.batchapplication.region.port.out;

import java.math.BigDecimal;
import java.util.List;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;

/**
 * 원천에서 읽어 온 행정동 하나.
 *
 * <p>도메인 타입({@code GeoPoint}·{@code GeoRing})을 담는 것은 batch-application이 domain-module을
 * 의존하는 정상 방향이며, 좌표 검증(위경도 범위·링 최소 정점 수)을 도메인 VO에 맡겨 어댑터가 같은 규칙을
 * 다시 구현하지 않게 한다.
 *
 * <p><b>이름에 {@code Result}가 아니라 {@code Source}를 쓰는 이유</b>: ceo-application이 이미
 * {@code com.tastyhouse.application.region.port.out} 패키지에 전혀 다른 계약인
 * {@code AdminDongBoundaryResult}를 소유하고 있다. 이름이 같으면 읽는 사람이 두 타입을 혼동하므로,
 * 이 타입은 원천(source) 데이터를 나타낸다는 의미로 {@code AdminDongBoundarySource}라 부른다.
 *
 * @param code     행정동 코드(행정안전부 행정기관코드 10자리)
 * @param sidoName 시/도 이름. 저장 관례에 맞춰 짧은 형태로 정규화된 값이다
 * @param center   경계 내부가 보장된 대표점
 * @param boundary 경계 링 목록
 */
public record AdminDongBoundarySource(
    String code,
    String sidoName,
    String sigunguName,
    String dongName,
    GeoPoint center,
    List<GeoRing> boundary
) {

    public AdminDongBoundarySource {
        boundary = boundary == null ? List.of() : List.copyOf(boundary);
    }

    public static AdminDongBoundarySource of(
        String code,
        String sidoName,
        String sigunguName,
        String dongName,
        BigDecimal centerLatitude,
        BigDecimal centerLongitude,
        List<GeoRing> boundary
    ) {
        return new AdminDongBoundarySource(
            code,
            sidoName,
            sigunguName,
            dongName,
            GeoPoint.of(centerLatitude, centerLongitude),
            boundary
        );
    }
}
