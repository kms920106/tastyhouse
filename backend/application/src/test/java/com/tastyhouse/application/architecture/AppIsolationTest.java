package com.tastyhouse.application.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * <b>챕터 01 신설</b> — 4개 앱 슬라이스가 서로를 참조하지 않음을 강제한다.
 *
 * <p>이것이 이 챕터에서 <b>가장 중요한 신설 규칙</b>이다. 모듈이 4개일 때는 앱 간 수평 의존을
 * <b>빌드 그래프가</b> 막았다 — {@code web-application}의 build.gradle에 {@code ceo-application}이
 * 없으니 참조하면 컴파일이 깨졌다. 4개를 한 모듈로 합치는 순간 그 게이트가 사라지고, 아무 앱이나
 * 다른 앱의 서비스를 import 해도 빌드가 통과한다. 잃어버린 컴파일 게이트를 규칙으로 대체한다.
 *
 * <p>4벌의 {@code LayerRulesTest}에 각각 있던 {@code shouldNotDependOnOtherApplicationModules}
 * (자기를 뺀 3개 앱 패키지를 열거해 금지)를 이 파일의 슬라이스 규칙 한 개로 대체했다.
 *
 * <p><b>importer는 4개 앱 패키지만 넣는다.</b> {@code com.tastyhouse.application..}(읽기 계약)을
 * 함께 넣으면 아래 {@code (*)application} 패턴이 <b>빈 접두어로 5번째 슬라이스</b>를 만들고,
 * 모든 앱이 계약에 정당하게 의존하므로 규칙이 즉시 실패한다. 계약은 공유되는 것이 맞으므로
 * 애초에 이 규칙의 대상이 아니다.
 */
class AppIsolationTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.webapplication", "com.tastyhouse.adminapplication",
                        "com.tastyhouse.ceoapplication", "com.tastyhouse.batchapplication");

    /**
     * 앱 간 수평 의존 금지.
     *
     * <p>앱이 공유해도 되는 것은 domain-module과 읽기 계약({@code com.tastyhouse.application..})뿐이다.
     * 다른 앱의 서비스·포트·Command를 참조하려는 시도는 앱 경계를 지우는 것이므로 막는다 —
     * {@code TokenService}·{@code AuthCommandService}처럼 이름이 같은 타입이 앱마다 따로 있는 것은
     * 의도된 중복이지 통합 대상이 아니다(backend/CLAUDE.md 허용 목록).
     */
    @Test
    void appsShouldNotDependOnEachOther() {
        SlicesRuleDefinition.slices().matching("com.tastyhouse.(*)application..")
            .should().notDependOnEachOther()
            .because("앱 간 수평 의존을 두지 않는다 — 공유되는 것은 domain-module과 읽기 계약뿐")
            .check(classes);
    }

    /**
     * 위 슬라이스 규칙의 anchor.
     *
     * <p>슬라이스 규칙은 슬라이스가 0개여도 조용히 통과하므로, 앱 패키지가 통째로 사라지거나
     * importer 범위가 잘못돼도 green이 될 수 있다. 정확히 4개임을 못 박아 그 경우를 잡는다.
     * (챕터 03에서 패키지가 평탄화되고 앱 소속이 마커 애노테이션으로 옮겨가면 이 규칙과 anchor는
     * 마커 기준으로 다시 쓴다.)
     */
    @Test
    void exactlyFourAppSlicesExist() {
        Set<String> apps = classes.stream()
            .map(c -> c.getPackageName().split("\\."))
            .filter(segments -> segments.length > 2)
            .map(segments -> segments[2])
            .filter(s -> s.endsWith("application"))
            .collect(Collectors.toSet());

        assertThat(apps)
            .as("앱 슬라이스가 4개가 아니면 notDependOnEachOther가 공허하게 통과할 수 있다")
            .containsExactlyInAnyOrder(
                "webapplication", "adminapplication", "ceoapplication", "batchapplication");
    }

}
