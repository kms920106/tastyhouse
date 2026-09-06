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

@Component
public class AdminDongBoundaryClient implements AdminDongBoundaryPort {

    private static final Logger log = LoggerFactory.getLogger(AdminDongBoundaryClient.class);

    private static final List<String> SIDO_SUFFIXES =
        List.of("특별자치도", "특별자치시", "광역시", "특별시", "자치도", "자치시");

    private final AdminDongBoundaryProperties properties;
    private final ObjectMapper objectMapper;

    public AdminDongBoundaryClient(AdminDongBoundaryProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

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

    private boolean moveToFeatures(JsonParser parser) throws IOException {
        while (parser.nextToken() != null) {
            if (parser.currentToken() == JsonToken.FIELD_NAME && "features".equals(parser.currentName())) {
                return parser.nextToken() == JsonToken.START_ARRAY;
            }
        }
        return false;
    }

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
                points.add(GeoPoint.of(
                    BigDecimal.valueOf(point.get(1).asDouble()),
                    BigDecimal.valueOf(point.get(0).asDouble())
                ));
            }

            try {
                target.add(GeoRing.of(points));
            } catch (IllegalArgumentException e) {
                log.debug("행정동 경계의 퇴화 링을 건너뜁니다: 정점 {}개", points.size());
            }
        }
    }

    private static String lastToken(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return null;
        }
        String[] tokens = fullName.trim().split("\\s+");
        return tokens[tokens.length - 1];
    }

    private static String shortSidoName(String sidoName) {
        for (String suffix : SIDO_SUFFIXES) {
            if (sidoName.endsWith(suffix) && sidoName.length() > suffix.length()) {
                return sidoName.substring(0, sidoName.length() - suffix.length());
            }
        }
        return sidoName;
    }
}
