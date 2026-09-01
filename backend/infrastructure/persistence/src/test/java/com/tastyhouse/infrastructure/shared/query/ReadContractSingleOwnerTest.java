package com.tastyhouse.infrastructure.shared.query;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 읽기 계약(FQCN)이 <b>정확히 한 모듈에서만</b> 정의되는지 검증하는 가드 테스트.
 *
 * <p><b>왜 필요한가</b>: {@code application-common-module} 해체(챕터 09)로 읽기 계약 소유가 5개 모듈로
 * 갈라졌다 — 한 앱만 쓰는 단독 계약은 그 앱의 {@code {앱}-application}이, 2개 이상이 쓰는 공유 계약은
 * {@code domain-module}이 소유한다. 그런데 <b>패키지는 다섯 모듈 모두 {@code com.tastyhouse.application.<ctx>.port.out}
 * 그대로다</b>(split package). 소비 측 import를 한 줄도 고치지 않기 위한 의도된 선택이지만, 그 대가로
 * "같은 FQCN이 두 모듈에 정의되어도 컴파일이 통과한다"는 구멍이 생긴다.
 *
 * <p>이 상태의 위험은 <b>조용하다는 것</b>이다. 두 모듈이 같은 클래스를 정의하면 어느 쪽이 로드될지는
 * 런타임 클래스패스 순서가 정한다. 컴파일도 통과하고 부팅도 성공하며, 두 정의가 미묘하게 다를 때
 * (예: record 컴포넌트 하나가 추가된 쪽과 아닌 쪽) 비로소 엉뚱한 곳에서 터진다. 계약을 옮기다가
 * 원본 삭제를 빠뜨리는 것이 전형적인 발생 경로다.
 *
 * <p><b>왜 이 모듈에 두는가</b>: {@code infrastructure:persistence}가 4개 application 모듈을
 * {@code implementation}으로 의존하고 {@code domain-module}을 {@code api}로 의존하므로(챕터 01),
 * 계약을 소유하는 5개 모듈이 전부 이 모듈의 테스트 런타임 클래스패스에 올라온다. 중복을 볼 수 있는
 * 유일한 지점이다.
 */
class ReadContractSingleOwnerTest {

    private static final String CONTRACT_PATTERN =
        "classpath*:com/tastyhouse/application/**/port/out/*.class";

    @Test
    @DisplayName("읽기 계약 FQCN은 정확히 한 모듈에서만 정의되어야 한다 (split package 중복 정의 검출)")
    void readContractsShouldHaveSingleOwner() {
        Map<String, List<String>> locationsByClassName = scanContractLocations();

        // 스캔이 아무것도 못 찾으면 규칙이 공허하게 통과하므로, 대상이 존재하는 것 자체를 먼저 검증한다.
        assertThat(locationsByClassName)
            .as("읽기 계약을 하나도 찾지 못했다 — 스캔 패턴(%s)이 잘못되었거나 "
                + "이 모듈이 계약 소유 모듈을 더 이상 의존하지 않을 수 있다", CONTRACT_PATTERN)
            .isNotEmpty();

        List<String> duplicated = locationsByClassName.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .map(entry -> "%s\n    → %s".formatted(entry.getKey(), String.join("\n    → ", entry.getValue())))
            .toList();

        assertThat(duplicated)
            .as("""
                아래 읽기 계약이 두 개 이상의 모듈에서 정의되어 있다. 계약 패키지는 5개 모듈이 공유하는
                split package라 같은 FQCN이 중복 정의되어도 컴파일이 통과하고, 어느 정의가 로드될지는
                런타임 클래스패스 순서가 정한다 — 두 정의가 다르면 조용히 한쪽이 이기고 엉뚱한 곳에서
                터진다. 계약은 소유 모듈 한 곳에만 둔다(단독 소비는 그 앱의 {앱}-application,
                2개 이상 앱이 소비하면 domain-module). 계약을 옮겼다면 원본이 남아 있지 않은지 확인한다.""")
            .isEmpty();
    }

    /**
     * 계약 FQCN별로 그것이 발견된 위치(jar 또는 클래스 디렉터리)를 모은다.
     *
     * <p>{@code classpath*:}는 클래스패스의 <b>모든</b> 엔트리를 훑으므로, 같은 FQCN이 두 모듈에 있으면
     * {@code Resource}가 두 개 돌아온다. 실패 메시지에 어느 모듈인지 보여야 조치가 가능하므로 URL을 함께 담는다.
     */
    private Map<String, List<String>> scanContractLocations() {
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resolver);

        Map<String, List<String>> locationsByClassName = new LinkedHashMap<>();
        try {
            for (Resource resource : resolver.getResources(CONTRACT_PATTERN)) {
                String className = metadataReaderFactory.getMetadataReader(resource)
                    .getClassMetadata()
                    .getClassName();
                // 중첩 클래스는 바이너리명에 '$'가 들어간다 — 최상위 타입만 센다.
                if (className.contains("$")) {
                    continue;
                }
                URL url = resource.getURL();
                locationsByClassName
                    .computeIfAbsent(className, key -> new ArrayList<>())
                    .add(url.toString());
            }
        } catch (IOException e) {
            throw new IllegalStateException("읽기 계약 클래스 스캔에 실패했다: " + CONTRACT_PATTERN, e);
        }

        return locationsByClassName;
    }
}
