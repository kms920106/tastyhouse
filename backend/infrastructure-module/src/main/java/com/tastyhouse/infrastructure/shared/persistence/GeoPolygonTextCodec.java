package com.tastyhouse.infrastructure.shared.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;

/**
 * 폴리곤 좌표를 텍스트 컬럼({@code LONGTEXT})에 담기 위한 인코딩·디코딩.
 *
 * <pre>
 * 링 구분 ";"  ·  점 구분 ","  ·  점 내부는 "경도 위도"(공백 1칸)
 * 소수점 6자리 고정
 *
 * 예) 127.036000 37.500000,127.040000 37.500000,127.040000 37.505000;127.038000 37.499000,...
 * </pre>
 *
 * <p><b>MySQL {@code GEOMETRY}를 쓰지 않는 이유</b>: 공간 인덱스가 이득을 주는 질의가 설계상 없다
 * (폴리곤 조회는 항상 {@code WHERE shop_id = ?} 단건이다). 반면 {@code hibernate-spatial}+JTS 의존,
 * dialect 교체, SRID 4326 축순서 함정, 자기교차 도형에서의 {@code ST_Contains} 미정의 동작을 모두
 * 떠안게 된다. domain-module은 production 의존이 0개로 강제되어 JTS {@code Geometry}를 도메인에 둘 수도 없다.
 *
 * <p><b>JSON을 쓰지 않는 이유</b>: infrastructure-module에 Jackson이 보장되지 않고 리포에 JSON 컬럼
 * 선례가 없다. {@code String.split}만으로 끝나는 형식이라 의존을 늘릴 이유가 없다.
 *
 * <p><b>좌표 순서가 "경도 위도"인 이유</b>: GeoJSON·WKT 등 공간 데이터 표준이 {@code (x, y) = (경도, 위도)}
 * 순서를 쓰므로 저장 형식은 그 관례를 따른다. 다만 <b>API 경계에서는 {@code {latitude, longitude}} 객체</b>로
 * 주고받아 순서 혼동을 없앤다 — 이 코덱은 그 경계 안쪽(영속 계층)에만 존재한다.
 */
public final class GeoPolygonTextCodec {

    /** 위경도 저장 정밀도({@code DECIMAL(9,6)})와 맞춘 소수 자릿수. */
    private static final int COORDINATE_SCALE = 6;

    private static final String RING_DELIMITER = ";";
    private static final String POINT_DELIMITER = ",";
    private static final String COORDINATE_DELIMITER = " ";

    private GeoPolygonTextCodec() {
    }

    /** 폴리곤을 저장용 문자열로 직렬화한다. */
    public static String encode(GeoPolygon polygon) {
        if (polygon == null) {
            throw new IllegalArgumentException("직렬화할 도형은 필수입니다.");
        }

        StringBuilder encoded = new StringBuilder();
        for (GeoRing ring : polygon.rings()) {
            if (!encoded.isEmpty()) {
                encoded.append(RING_DELIMITER);
            }
            appendRing(encoded, ring);
        }
        return encoded.toString();
    }

    /** 링 목록(행정동 경계)을 저장용 문자열로 직렬화한다. */
    public static String encodeRings(List<GeoRing> rings) {
        if (rings == null || rings.isEmpty()) {
            return null;
        }
        return encode(GeoPolygon.of(rings));
    }

    /**
     * 저장된 문자열을 폴리곤으로 복원한다.
     *
     * <p>형식이 깨진 입력은 {@link IllegalArgumentException}으로 실패한다 — 조용히 건너뛰면 도형의 일부가
     * 사라진 채 복원되어, 점주가 그린 것과 다른 배달지역이 저장된 것처럼 보인다.
     */
    public static GeoPolygon decode(String encoded) {
        List<GeoRing> rings = decodeRings(encoded);
        if (rings.isEmpty()) {
            throw new IllegalArgumentException("도형 문자열이 비어 있습니다.");
        }
        return GeoPolygon.of(rings);
    }

    /**
     * 저장된 문자열을 링 목록으로 복원한다. 값이 없으면(미보유) 빈 목록을 반환한다 — 행정동 경계는
     * 단계적으로 투입되므로 미보유가 정상 상태다.
     */
    public static List<GeoRing> decodeRings(String encoded) {
        if (encoded == null || encoded.isBlank()) {
            return List.of();
        }

        List<GeoRing> rings = new ArrayList<>();
        for (String ringToken : encoded.split(RING_DELIMITER)) {
            if (ringToken.isBlank()) {
                continue;
            }
            rings.add(decodeRing(ringToken));
        }
        return rings;
    }

    private static void appendRing(StringBuilder target, GeoRing ring) {
        boolean first = true;
        for (GeoPoint point : ring.points()) {
            if (!first) {
                target.append(POINT_DELIMITER);
            }
            target.append(scaled(point.longitude()))
                .append(COORDINATE_DELIMITER)
                .append(scaled(point.latitude()));
            first = false;
        }
    }

    private static GeoRing decodeRing(String ringToken) {
        String[] pointTokens = ringToken.split(POINT_DELIMITER);
        List<GeoPoint> points = new ArrayList<>(pointTokens.length);

        for (String pointToken : pointTokens) {
            String trimmed = pointToken.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            points.add(decodePoint(trimmed));
        }
        return GeoRing.of(points);
    }

    private static GeoPoint decodePoint(String pointToken) {
        String[] coordinates = pointToken.split(COORDINATE_DELIMITER);
        if (coordinates.length != 2) {
            throw new IllegalArgumentException("좌표 형식이 올바르지 않습니다(\"경도 위도\" 형태여야 합니다): " + pointToken);
        }

        try {
            // 저장 형식은 "경도 위도" 순서이고 GeoPoint는 (위도, 경도) 순서다 — 여기서 뒤집는다.
            return GeoPoint.of(new BigDecimal(coordinates[1]), new BigDecimal(coordinates[0]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("좌표를 숫자로 해석할 수 없습니다: " + pointToken, e);
        }
    }

    private static String scaled(BigDecimal value) {
        return value.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP).toPlainString();
    }
}
