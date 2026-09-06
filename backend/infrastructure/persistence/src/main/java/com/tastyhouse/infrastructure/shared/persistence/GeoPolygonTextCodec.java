package com.tastyhouse.infrastructure.shared.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;

public final class GeoPolygonTextCodec {
    private static final int COORDINATE_SCALE = 6;

    private static final String RING_DELIMITER = ";";
    private static final String POINT_DELIMITER = ",";
    private static final String COORDINATE_DELIMITER = " ";

    private GeoPolygonTextCodec() {
    }

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

    public static String encodeRings(List<GeoRing> rings) {
        if (rings == null || rings.isEmpty()) {
            return null;
        }
        return encode(GeoPolygon.of(rings));
    }

    public static GeoPolygon decode(String encoded) {
        List<GeoRing> rings = decodeRings(encoded);
        if (rings.isEmpty()) {
            throw new IllegalArgumentException("도형 문자열이 비어 있습니다.");
        }
        return GeoPolygon.of(rings);
    }

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
            return GeoPoint.of(new BigDecimal(coordinates[1]), new BigDecimal(coordinates[0]));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("좌표를 숫자로 해석할 수 없습니다: " + pointToken, e);
        }
    }

    private static String scaled(BigDecimal value) {
        return value.setScale(COORDINATE_SCALE, RoundingMode.HALF_UP).toPlainString();
    }
}
