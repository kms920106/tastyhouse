package com.tastyhouse.infrastructure.shared.persistence;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoPolygon;
import com.tastyhouse.domain.shared.geo.GeoRing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 도형 텍스트 인코딩 단위 테스트.
 *
 * <p>가장 중요한 검증은 <b>왕복 동일성</b>과 <b>좌표 순서</b>다 — 저장 형식은 "경도 위도"이고 도메인
 * 타입은 (위도, 경도) 순서라, 한쪽만 틀리면 컴파일은 통과하고 도형이 지구 반대편에 그려진다.
 */
class GeoPolygonTextCodecTest {

    @Test
    @DisplayName("인코딩 후 디코딩하면 원본과 같은 좌표가 나온다(왕복 동일성)")
    void encodeDecode_roundTrips() {
        GeoPolygon original = GeoPolygon.of(List.of(ring(
            "37.500000", "127.036000",
            "37.500000", "127.040000",
            "37.505000", "127.040000"
        )));

        GeoPolygon decoded = GeoPolygonTextCodec.decode(GeoPolygonTextCodec.encode(original));

        assertThat(decoded.rings()).hasSize(1);
        assertThat(decoded.rings().getFirst().points())
            .extracting(point -> point.latitude().toPlainString() + "/" + point.longitude().toPlainString())
            .containsExactly("37.500000/127.036000", "37.500000/127.040000", "37.505000/127.040000");
    }

    @Test
    @DisplayName("저장 형식은 \"경도 위도\" 순서다")
    void encode_writesLongitudeFirst() {
        GeoPolygon polygon = GeoPolygon.of(List.of(ring(
            "37.500000", "127.036000",
            "37.501000", "127.037000",
            "37.502000", "127.038000"
        )));

        String encoded = GeoPolygonTextCodec.encode(polygon);

        assertThat(encoded).startsWith("127.036000 37.500000,");
    }

    @Test
    @DisplayName("링이 여러 개면 세미콜론으로 구분한다")
    void encodeDecode_handlesMultipleRings() {
        GeoPolygon polygon = GeoPolygon.of(List.of(
            ring("37.500000", "127.036000", "37.501000", "127.037000", "37.502000", "127.038000"),
            ring("37.600000", "127.136000", "37.601000", "127.137000", "37.602000", "127.138000")
        ));

        String encoded = GeoPolygonTextCodec.encode(polygon);

        assertThat(encoded).contains(";");
        assertThat(GeoPolygonTextCodec.decode(encoded).rings()).hasSize(2);
    }

    @Test
    @DisplayName("소수 6자리로 고정 절단한다")
    void encode_fixesScaleToSixDecimals() {
        GeoPolygon polygon = GeoPolygon.of(List.of(ring(
            "37.5", "127.0",
            "37.5000004", "127.0000004",
            "37.51", "127.01"
        )));

        String encoded = GeoPolygonTextCodec.encode(polygon);

        assertThat(encoded).startsWith("127.000000 37.500000,");
        assertThat(encoded).contains("127.000000 37.500000,127.000000 37.500000");
    }

    @Test
    @DisplayName("빈 문자열·null은 빈 링 목록으로 정규화한다(경계 미보유는 정상 상태)")
    void decodeRings_treatsBlankAsEmpty() {
        assertThat(GeoPolygonTextCodec.decodeRings(null)).isEmpty();
        assertThat(GeoPolygonTextCodec.decodeRings("")).isEmpty();
        assertThat(GeoPolygonTextCodec.decodeRings("   ")).isEmpty();
    }

    @Test
    @DisplayName("깨진 토큰은 조용히 건너뛰지 않고 예외로 실패한다")
    void decode_rejectsMalformedInput() {
        assertThatThrownBy(() -> GeoPolygonTextCodec.decode("127.036000"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GeoPolygonTextCodec.decode("abc def,127.0 37.5,127.1 37.6"))
            .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> GeoPolygonTextCodec.decode(""))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("정점 5000개도 왕복한다")
    void encodeDecode_handlesMaxVertices() {
        List<GeoPoint> points = new ArrayList<>(5000);
        for (int i = 0; i < 5000; i++) {
            points.add(GeoPoint.of(
                new BigDecimal("37.500000").add(BigDecimal.valueOf(i).movePointLeft(6)),
                new BigDecimal("127.000000")
            ));
        }
        GeoPolygon polygon = GeoPolygon.of(List.of(GeoRing.of(points)));

        GeoPolygon decoded = GeoPolygonTextCodec.decode(GeoPolygonTextCodec.encode(polygon));

        assertThat(decoded.vertexCount()).isEqualTo(5000);
    }

    /** {@code lat, lng} 문자열 쌍으로 링을 만든다. */
    private static GeoRing ring(String... latLngPairs) {
        List<GeoPoint> points = new ArrayList<>();
        for (int i = 0; i < latLngPairs.length; i += 2) {
            points.add(GeoPoint.of(new BigDecimal(latLngPairs[i]), new BigDecimal(latLngPairs[i + 1])));
        }
        return GeoRing.of(points);
    }
}
