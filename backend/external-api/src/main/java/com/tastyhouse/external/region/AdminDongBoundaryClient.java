package com.tastyhouse.external.region;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.tastyhouse.application.region.port.out.AdminDongBoundaryPort;
import com.tastyhouse.application.region.port.out.AdminDongBoundarySource;
import com.tastyhouse.domain.shared.geo.GeoPoint;
import com.tastyhouse.domain.shared.geo.GeoRing;
import com.tastyhouse.domain.shared.geo.InteriorPoint;
import com.tastyhouse.external.exception.ExternalApiErrorCode;
import com.tastyhouse.external.exception.ExternalApiException;

/**
 * 행정동 경계 GeoJSON 원천 클라이언트.
 *
 * <p>원천은 통계청 SGIS 행정동 경계를 행정구역 변경 이력에 맞춰 보정하고 WGS84 GeoJSON으로 정리한
 * 공개 데이터셋이다(CC BY 4.0, 출처 표시 시 상업적 이용 허용). SGIS 원본은 SHP + EPSG:5179라 좌표계
 * 변환이 필요한데, 이 원천은 이미 <b>EPSG:4326(WGS84)</b>이라 그대로 쓸 수 있다.
 *
 * <p><b>WebClient가 아니라 {@link HttpClient}를 쓰는 이유</b>: 응답이 30MB대 단일 JSON이라
 * {@code bodyToMono(String.class)}로 받으면 문자열 하나로 힙에 통째 올라간다. {@code InputStream}으로
 * 받아 Jackson 스트리밍 파서로 feature 하나씩 소비하면 전체 문서를 메모리에 올리지 않는다.
 *
 * <p><b>{@code sidoName} 정규화</b>: 원천은 {@code "서울특별시"} 같은 정식 명칭을 쓰지만, 이 저장소의
 * 주소 데이터는 {@code "서울 강남구 테헤란로 152"}처럼 짧은 형태다. 행정동 매칭이 주소 문자열 토큰과
 * {@code sido_name}을 직접 비교하므로(회원 배달주소의 행정동 채우기) 저장 시점에 짧은 형태로 맞춘다.
 */
@Component
public class AdminDongBoundaryClient implements AdminDongBoundaryPort {

    private static final Logger log = LoggerFactory.getLogger(AdminDongBoundaryClient.class);

    /** 원천이 쓰는 정식 시/도 명칭 → 주소 데이터가 쓰는 짧은 명칭. */
    private static final List<String> SIDO_SUFFIXES =
        List.of("특별자치도", "특별자치시", "광역시", "특별시", "자치도", "자치시");

    private final AdminDongBoundaryProperties properties;
    private final ObjectMapper objectMapper;

    public AdminDongBoundaryClient(AdminDongBoundaryProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /**
     * 원천에서 전국 행정동 경계를 읽어 온다.
     *
     * <p>대표점을 만들지 못한 행(경계가 깨졌거나 링 정점이 부족한 경우)은 <b>건너뛰고 로그만 남긴다</b> —
     * 한 동 때문에 전국 동기화를 실패시키는 것보다 그 동만 빠지는 편이 낫고, 빠진 동은 다음 동기화에서
     * 원천이 고쳐지면 자연히 복구된다.
     */
    @Override
    public List<AdminDongBoundarySource> fetchAll() {
        HttpRequest request = HttpRequest.newBuilder(sourceUri())
            .timeout(Duration.ofSeconds(properties.timeoutSeconds()))
            .GET()
            .build();

        try (HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(properties.timeoutSeconds()))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build()) {
            HttpResponse<InputStream> response =
                client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            if (response.statusCode() != 200) {
                log.error("행정동 경계 원천 응답이 비정상입니다: status={}, url={}",
                    response.statusCode(), properties.sourceUrl());
                throw new ExternalApiException(ExternalApiErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED);
            }

            try (InputStream body = new BoundedInputStream(response.body(), properties.maxBytes())) {
                return parseFeatures(body);
            }
        } catch (IOException e) {
            log.error("행정동 경계 원천 다운로드에 실패했습니다: url={}", properties.sourceUrl(), e);
            throw new ExternalApiException(ExternalApiErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ExternalApiException(ExternalApiErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED);
        }
    }

    private URI sourceUri() {
        try {
            return new URI(properties.sourceUrl());
        } catch (URISyntaxException e) {
            throw new ExternalApiException(ExternalApiErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED);
        }
    }

    /** {@code features} 배열만 스트리밍으로 훑어 하나씩 변환한다. */
    private List<AdminDongBoundarySource> parseFeatures(InputStream body) throws IOException {
        List<AdminDongBoundarySource> results = new ArrayList<>();
        int skipped = 0;

        try (JsonParser parser = objectMapper.getFactory().createParser(body)) {
            if (!moveToFeatures(parser)) {
                log.error("행정동 경계 GeoJSON에 features 배열이 없습니다: url={}", properties.sourceUrl());
                throw new ExternalApiException(ExternalApiErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED);
            }

            while (parser.nextToken() == JsonToken.START_OBJECT) {
                AdminDongBoundarySource result = toResult(objectMapper.readTree(parser));
                if (result == null) {
                    skipped++;
                    continue;
                }
                results.add(result);
            }
        }

        if (results.isEmpty()) {
            log.error("행정동 경계 원천에서 읽어 온 행이 없습니다: url={}", properties.sourceUrl());
            throw new ExternalApiException(ExternalApiErrorCode.ADMIN_DONG_BOUNDARY_FETCH_FAILED);
        }

        log.info("행정동 경계 원천 파싱 완료: {}건 (대표점 계산 실패로 제외 {}건)", results.size(), skipped);
        return results;
    }

    /** {@code features} 배열의 시작 토큰까지 커서를 옮긴다. */
    private boolean moveToFeatures(JsonParser parser) throws IOException {
        while (parser.nextToken() != null) {
            if (parser.currentToken() == JsonToken.FIELD_NAME && "features".equals(parser.currentName())) {
                return parser.nextToken() == JsonToken.START_ARRAY;
            }
        }
        return false;
    }

    /** GeoJSON feature 하나를 결과로 변환한다. 대표점을 만들지 못하면 {@code null}. */
    private AdminDongBoundarySource toResult(JsonNode feature) {
        JsonNode properties = feature.path("properties");
        String code = properties.path("adm_cd2").asText(null);
        String sidoName = properties.path("sidonm").asText(null);
        String sigunguName = properties.path("sggnm").asText(null);
        String dongName = lastToken(properties.path("adm_nm").asText(null));

        if (code == null || sidoName == null || sigunguName == null || dongName == null) {
            return null;
        }

        List<GeoRing> boundary = toRings(feature.path("geometry"));
        GeoPoint center = InteriorPoint.of(boundary);
        if (center == null) {
            log.warn("행정동 대표점을 계산하지 못해 건너뜁니다: code={}, name={}", code, dongName);
            return null;
        }

        return new AdminDongBoundarySource(
            code,
            shortSidoName(sidoName),
            sigunguName,
            dongName,
            center,
            boundary
        );
    }

    /**
     * GeoJSON {@code MultiPolygon}/{@code Polygon} 좌표를 링 목록으로 편다.
     *
     * <p>여러 폴리곤(본토 + 부속 섬)을 <b>하나의 링 목록으로 합친다</b> — 이 저장 형식이 링 목록만
     * 표현하기 때문이다. 대표점은 {@link InteriorPoint}가 첫 링(가장 먼저 나오는 외곽)을 기준으로 잡는다.
     */
    private List<GeoRing> toRings(JsonNode geometry) {
        String type = geometry.path("type").asText("");
        JsonNode coordinates = geometry.path("coordinates");

        List<GeoRing> rings = new ArrayList<>();
        if ("MultiPolygon".equals(type)) {
            for (JsonNode polygon : coordinates) {
                appendPolygonRings(polygon, rings);
            }
        } else if ("Polygon".equals(type)) {
            appendPolygonRings(coordinates, rings);
        }
        return rings;
    }

    private void appendPolygonRings(JsonNode polygon, List<GeoRing> target) {
        for (JsonNode ring : polygon) {
            List<GeoPoint> points = new ArrayList<>();
            for (JsonNode point : ring) {
                if (point.size() < 2) {
                    continue;
                }
                // GeoJSON은 [경도, 위도] 순서이고 GeoPoint는 (위도, 경도) 순서다 — 여기서 뒤집는다.
                points.add(GeoPoint.of(
                    BigDecimal.valueOf(point.get(1).asDouble()),
                    BigDecimal.valueOf(point.get(0).asDouble())
                ));
            }

            try {
                target.add(GeoRing.of(points));
            } catch (IllegalArgumentException e) {
                // 정점이 3개 미만인 퇴화 링은 면을 이루지 못하므로 버린다.
                log.debug("행정동 경계의 퇴화 링을 건너뜁니다: 정점 {}개", points.size());
            }
        }
    }

    /** {@code "서울특별시 종로구 사직동"} → {@code "사직동"}. */
    private static String lastToken(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        String[] tokens = fullName.trim().split("\\s+");
        return tokens[tokens.length - 1];
    }

    /** {@code "서울특별시"} → {@code "서울"}. 접미어가 없으면 원래 값 그대로 둔다(예: {@code "제주"}). */
    private static String shortSidoName(String sidoName) {
        for (String suffix : SIDO_SUFFIXES) {
            if (sidoName.endsWith(suffix) && sidoName.length() > suffix.length()) {
                return sidoName.substring(0, sidoName.length() - suffix.length());
            }
        }
        return sidoName;
    }
}
