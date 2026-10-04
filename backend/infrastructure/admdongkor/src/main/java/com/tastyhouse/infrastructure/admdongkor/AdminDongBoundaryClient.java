package com.tastyhouse.infrastructure.admdongkor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.tastyhouse.application.region.port.out.AdminDongBoundaryFetchResult;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryPort;
import com.tastyhouse.application.region.port.out.AdminDongBoundarySource;
import com.tastyhouse.application.region.port.out.BoundaryCoordinate;
import com.tastyhouse.application.region.port.out.BoundaryRing;
import com.tastyhouse.infrastructure.restclient.HttpRequestFactories;

@Component
public class AdminDongBoundaryClient implements AdminDongBoundaryPort {

    private static final Logger log = LoggerFactory.getLogger(AdminDongBoundaryClient.class);

    private static final List<String> SIDO_SUFFIXES =
        List.of("특별자치도", "특별자치시", "광역시", "특별시", "자치도", "자치시");

    private final RestClient restClient;
    private final AdminDongBoundaryProperties properties;
    private final ObjectMapper objectMapper;

    public AdminDongBoundaryClient(
        RestClient.Builder restClientBuilder,
        AdminDongBoundaryProperties properties,
        ObjectMapper objectMapper
    ) {
        Duration timeout = Duration.ofSeconds(properties.timeoutSeconds());
        this.restClient = restClientBuilder
            .requestFactory(HttpRequestFactories.withTimeouts(timeout, timeout))
            .build();
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    public AdminDongBoundaryFetchResult fetchAll() {
        Optional<URI> sourceUri = sourceUri();
        if (sourceUri.isEmpty()) {
            return AdminDongBoundaryFetchResult.fetchFailed();
        }
        try {
            return restClient.get()
                .uri(sourceUri.get())
                .exchange((request, response) -> {
                    if (response.getStatusCode().value() != 200) {
                        log.error("행정동 경계 원천 응답이 비정상입니다: status={}, url={}",
                            response.getStatusCode().value(), properties.sourceUrl());
                        return AdminDongBoundaryFetchResult.fetchFailed();
                    }

                    try (InputStream body = new BoundedInputStream(response.getBody(), properties.maxBytes())) {
                        return parseFeatures(body);
                    }
                });
        } catch (ResourceAccessException e) {
            log.error("행정동 경계 원천 다운로드에 실패했습니다: url={}", properties.sourceUrl(), e);
            return AdminDongBoundaryFetchResult.fetchFailed();
        }
    }

    private Optional<URI> sourceUri() {
        try {
            return Optional.of(new URI(properties.sourceUrl()));
        } catch (URISyntaxException e) {
            log.error("행정동 경계 원천 URL 형식이 올바르지 않습니다: url={}", properties.sourceUrl(), e);
            return Optional.empty();
        }
    }

    private AdminDongBoundaryFetchResult parseFeatures(InputStream body) throws IOException {
        List<AdminDongBoundarySource> results = new ArrayList<>();
        int skipped = 0;

        try (JsonParser parser = objectMapper.getFactory().createParser(body)) {
            if (!moveToFeatures(parser)) {
                log.error("행정동 경계 GeoJSON에 features 배열이 없습니다: url={}", properties.sourceUrl());
                return AdminDongBoundaryFetchResult.fetchFailed();
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
            return AdminDongBoundaryFetchResult.fetchFailed();
        }

        log.info("행정동 경계 원천 파싱 완료: {}건 (필수 속성 누락으로 제외 {}건)", results.size(), skipped);
        return AdminDongBoundaryFetchResult.fetched(results);
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

        return new AdminDongBoundarySource(
            code,
            shortSidoName(sidoName),
            sigunguName,
            dongName,
            toRings(feature.path("geometry"))
        );
    }

    private List<BoundaryRing> toRings(JsonNode geometry) {
        String type = geometry.path("type").asText("");
        JsonNode coordinates = geometry.path("coordinates");

        List<BoundaryRing> rings = new ArrayList<>();
        if ("MultiPolygon".equals(type)) {
            for (JsonNode polygon : coordinates) {
                appendPolygonRings(polygon, rings);
            }
        } else if ("Polygon".equals(type)) {
            appendPolygonRings(coordinates, rings);
        }
        return rings;
    }

    private void appendPolygonRings(JsonNode polygon, List<BoundaryRing> target) {
        for (JsonNode ring : polygon) {
            List<BoundaryCoordinate> coordinates = new ArrayList<>();
            for (JsonNode point : ring) {
                if (point.size() < 2) {
                    continue;
                }
                coordinates.add(new BoundaryCoordinate(point.get(1).asDouble(), point.get(0).asDouble()));
            }
            target.add(new BoundaryRing(coordinates));
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
