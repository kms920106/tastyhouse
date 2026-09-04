package com.tastyhouse.application.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link LayerRulesTest}의 각 규칙이 <b>공허하게 통과하지 않음</b>을 보장한다.
 *
 * <p>이 저장소는 {@code allowEmptyShould(true)}를 금지한다 — 규칙이 대상을 잃으면 지우거나 anchor를
 * 고친다는 원칙이다. 그런데 {@code noClasses().that()...} 형태는 대상이 0건이어도 조용히 통과하므로,
 * 원칙을 지켰는지가 사람 눈에만 의존한다. 이 테스트가 그 지점을 자동화한다.
 *
 * <p><b>챕터 03 개정 — anchor가 앱별에서 모듈 합계로 바뀌었다.</b> 과거에는 앱별 패키지
 * ({@code com.tastyhouse.{app}application})로 나눠 세어, 한 앱의 클래스가 통째로 사라져도 나머지 세
 * 앱이 합계 하한을 떠받치는 것을 막았다. 평탄화로 그 구분이 사라졌으므로 여기서는 모듈 합계만 세고,
 * <b>앱별 소실은 {@link AppIsolationTest}의 마커별 anchor</b>({@code markerBeanCounts}·
 * {@code markerUseCaseCounts})가 승계해 잡는다. 마커가 앱 소속의 새 근거이기 때문이다.
 *
 * <p>batch의 anchor는 {@link BatchSchedulerRulesTest}가 갖는다(규모가 작아 하한이 아니라 정확히 일치).
 *
 * <p>기대값은 <b>하한</b>으로 둔다. 컨텍스트가 늘어나는 것은 정상이므로 정확히 일치를 요구하면
 * 기능 추가마다 이 파일을 고쳐야 하고, 그러면 anchor가 규칙이 아니라 잡음이 된다. 반대로 대량
 * 소실은 하한으로 충분히 잡힌다.
 */
class RuleAnchorTest {

    /** {@link LayerRulesTest}가 쓰는 importer와 동일 범위(챕터 03 — 하나로 합쳐졌다). */
    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    private long countSuffix(String suffix) {
        return classes.stream().filter(c -> c.getSimpleName().endsWith(suffix)).count();
    }

    /**
     * {@code commandServicesShouldNotDependOnQueryDaos} 등 CommandService 대상 규칙 4종의 anchor.
     *
     * <p>하한 91은 통합 전 3개 앱의 합이다(web 17 + admin 30 + ceo 44). batch는 CQRS를 쓰지 않아
     * {@code *CommandService}가 0개이므로 대상이 아니다.
     */
    @Test
    void commandServicesExist() {
        assertThat(countSuffix("CommandService"))
            .as("*CommandService가 0건이면 CommandService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(91);
    }

    /**
     * {@code queryServicesShouldNotDependOnWritePorts} · {@code queryServicesShouldImplementUseCase}의 anchor.
     *
     * <p>하한 100은 통합 전 3개 앱의 합이다(web 29 + admin 28 + ceo 43).
     */
    @Test
    void queryServicesExist() {
        assertThat(countSuffix("QueryService"))
            .as("*QueryService가 0건이면 QueryService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(100);
    }

    /**
     * {@code commandRecordsShouldBeBoundaryTyped} · {@code portInShouldNotDependOnWebPlumbing} 등의 anchor.
     *
     * <p>하한 556은 통합 전 4개 앱의 합이다(web 99 + admin 228 + ceo 222 + batch 7).
     */
    @Test
    void inboundPortsExist() {
        assertThat(classes.stream().filter(c -> resideInAPackage("..port.in..").test(c)).count())
            .as("..port.in..이 0건이면 경계 타입·web 플럼빙 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(556);
    }

    /**
     * 모듈 전체를 대상으로 하는 규칙들({@code applicationMustBeServletFree} ·
     * {@code applicationMustNotDependOnAdapters} · {@code shouldNotDependOnQuerydsl} ·
     * {@code shouldNotDependOnInfrastructure} · {@code applicationShouldNotDependOnSwagger} ·
     * {@code applicationShouldNotDependOnApiCommon})의 anchor.
     *
     * <p>하한 852는 통합 전 4개 앱의 합이다(web 200 + admin 290 + ceo 334 + batch 28). 각 값은
     * Response 승격(챕터 06·09·10)으로 재기준된 뒤의 것이다. 이 합계가 한 앱의 소실을 못 잡는 것은
     * 위 클래스 Javadoc대로 {@link AppIsolationTest}의 마커별 anchor가 보완한다.
     */
    @Test
    void moduleIsNotEmpty() {
        assertThat(classes.size())
            .as("모듈이 비면 noClasses() 전역 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(852);
    }

    /**
     * {@code readContractsShouldBeFrameworkFree}의 anchor.
     *
     * <p>하한 227은 통합 전 4개 앱의 합이다(web 85 + admin 79 + ceo 61 + batch 2). 챕터 03으로 규칙
     * 대상이 읽기 계약에서 {@code port.out} 전체(아웃바운드 SPI·Command 반환 record 포함)로 넓어졌으나,
     * 하한은 그대로 둔다 — 넓어진 만큼 실측이 늘어 하한이 더 여유로워질 뿐이고, 이 anchor가 잡으려는
     * 것은 대량 소실이기 때문이다.
     *
     * <p>{@code isOwnedByThisModule} 필터는 유지한다 — 아직 챕터 04 전이라 domain-module의 공유 계약
     * 55개가 split package로 함께 잡히고, 그 55개가 하한을 떠받쳐 주면 정작 이 모듈의 계약이 사라져도
     * anchor가 통과한다.
     *
     * <p><b>챕터 03 — 판별 방법이 바뀌었다.</b> 과거에는 "jar에서 왔으면 남의 것"으로 갈랐다(Gradle이
     * 프로젝트 의존은 jar로, 자기 산출물은 {@code build/classes} 디렉터리로 올리기 때문). 이 챕터에서
     * {@code java-test-fixtures} 플러그인을 적용하자({@link AppOwnership}을 api 모듈과 공유하려고)
     * <b>자기 모듈 산출물도 {@code application-0.0.1-SNAPSHOT.jar}로 올라오게 되어</b> 그 필터가
     * 전부를 걸러냈다 — anchor가 0을 세고 실패했다. 그래서 "jar 여부"가 아니라 <b>어느 jar인지</b>로
     * 판별한다.
     */
    @Test
    void readContractsExist() {
        assertThat(classes.stream()
            .filter(c -> resideInAPackage("..port.out..").test(c))
            .filter(RuleAnchorTest::isOwnedByThisModule)
            .count())
            .as("아웃바운드 계약이 0건이면 프레임워크-프리 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(227);
    }

    /**
     * 이 모듈이 <b>직접 소유</b>한 계약인지 판별한다.
     *
     * <p>판별 근거는 소스 위치다 — Gradle은 프로젝트 의존을 <b>jar</b>로 올리고 자기 모듈 산출물만
     * {@code build/classes/java/main} <b>디렉터리</b>로 올리므로, jar에서 온 것을 걸러내면 자기 소유분만 남는다.
     *
     * <p><b>챕터 04에서 이 필터를 지운다</b> — 공유 계약 55개가 domain-module에서 이 모듈로 돌아오면
     * 모든 계약이 같은 소스에서 오므로 구분할 것이 없어지고, 하한을 282(227+55)로 올리면 된다.
     */
    private static boolean isOwnedByThisModule(JavaClass contract) {
        return contract.getSource()
            .map(source -> source.getUri().toString())
            .filter(uri -> uri.contains("/application/build/") || !uri.contains(".jar"))
            .isPresent();
    }
}
