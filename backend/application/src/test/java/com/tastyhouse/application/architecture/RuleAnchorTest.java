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
 * 원칙을 지켰는지가 사람 눈에만 의존한다. 이 테스트가 그 지점을 자동화한다 — 챕터 01로 클래스가
 * 모듈 사이를 대량으로 옮겨 다닌 직후라 특히 필요하다(대상이 통째로 사라져도 빌드는 green이었다).
 *
 * <p><b>anchor는 앱별로 유지한다.</b> 4개 모듈이 한 모듈로 합쳐졌지만 자바 패키지는 앱별로 남아 있고
 * ({@code com.tastyhouse.{app}application}), 앱별로 세지 않으면 한 앱의 클래스가 통째로 사라져도
 * 나머지 세 앱이 합계 하한을 떠받쳐 anchor가 조용히 통과한다. 챕터 03에서 패키지가 평탄화되면
 * 마커 애노테이션 기준으로 다시 쓴다.
 *
 * <p>batch의 anchor는 {@link BatchSchedulerRulesTest}가 갖는다(규모가 작아 하한이 아니라 정확히 일치).
 *
 * <p>기대값은 <b>하한</b>으로 둔다. 컨텍스트가 늘어나는 것은 정상이므로 정확히 일치를 요구하면
 * 기능 추가마다 이 파일을 고쳐야 하고, 그러면 anchor가 규칙이 아니라 잡음이 된다. 반대로 대량
 * 소실은 하한으로 충분히 잡힌다.
 */
class RuleAnchorTest {

    private final JavaClasses web = importApp("com.tastyhouse.webapplication");
    private final JavaClasses admin = importApp("com.tastyhouse.adminapplication");
    private final JavaClasses ceo = importApp("com.tastyhouse.ceoapplication");
    private final JavaClasses batch = importApp("com.tastyhouse.batchapplication");

    /** {@code LayerRulesTest#readContractsShouldBeFrameworkFree}가 쓰는 importer와 동일 범위. */
    private final JavaClasses readContracts = importApp("com.tastyhouse.application");

    private static JavaClasses importApp(String pkg) {
        return new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(pkg);
    }

    private static long countSuffix(JavaClasses classes, String suffix) {
        return classes.stream().filter(c -> c.getSimpleName().endsWith(suffix)).count();
    }

    private static long countInboundPorts(JavaClasses classes) {
        return classes.stream().filter(c -> resideInAPackage("..port.in..").test(c)).count();
    }

    /**
     * {@code commandServicesShouldNotDependOnQueryDaos} 등 CommandService 대상 규칙 4종의 anchor.
     *
     * <p>batch는 CQRS를 쓰지 않아 {@code *CommandService}가 0개이므로 대상이 아니다.
     */
    @Test
    void commandServicesExist() {
        assertThat(countSuffix(web, "CommandService"))
            .as("web *CommandService가 0건이면 CommandService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(17);
        assertThat(countSuffix(admin, "CommandService"))
            .as("admin *CommandService가 0건이면 CommandService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(30);
        assertThat(countSuffix(ceo, "CommandService"))
            .as("ceo *CommandService가 0건이면 CommandService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(44);
    }

    /** {@code queryServicesShouldNotDependOnWritePorts} · {@code queryServicesShouldImplementUseCase}의 anchor. */
    @Test
    void queryServicesExist() {
        assertThat(countSuffix(web, "QueryService"))
            .as("web *QueryService가 0건이면 QueryService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(29);
        assertThat(countSuffix(admin, "QueryService"))
            .as("admin *QueryService가 0건이면 QueryService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(28);
        assertThat(countSuffix(ceo, "QueryService"))
            .as("ceo *QueryService가 0건이면 QueryService 대상 규칙들이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(43);
    }

    /** {@code commandRecordsShouldBeBoundaryTyped} · {@code portInShouldNotDependOnWebPlumbing} 등의 anchor. */
    @Test
    void inboundPortsExist() {
        assertThat(countInboundPorts(web))
            .as("web ..port.in..이 0건이면 경계 타입·web 플럼빙 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(99);
        assertThat(countInboundPorts(admin))
            .as("admin ..port.in..이 0건이면 경계 타입·web 플럼빙 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(228);
        assertThat(countInboundPorts(ceo))
            .as("ceo ..port.in..이 0건이면 경계 타입·web 플럼빙 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(222);
    }

    /**
     * 모듈 전체를 대상으로 하는 규칙들({@code applicationMustBeServletFree} ·
     * {@code applicationMustNotDependOnAdapters} · {@code shouldNotDependOnQuerydsl} ·
     * {@code shouldNotDependOnInfrastructure} · {@code applicationShouldNotDependOnSwagger} ·
     * {@code applicationShouldNotDependOnApiCommon})의 anchor.
     *
     * <p>그 규칙들은 {@code noClasses()}로 <b>모듈 전체</b>를 대상으로 하므로 별도 anchor를 두지
     * 않는다 — 이 테스트가 전부의 anchor를 겸한다.
     *
     * <p>하한은 통합 전 각 모듈이 갖고 있던 값을 그대로 승계한다(web 200 · admin 290 · ceo 334 ·
     * batch 28). 합계 하나로 뭉뜽그리지 않는 이유는 위 클래스 Javadoc에 적었다 — 한 앱이 통째로
     * 사라져도 나머지가 합계를 떠받치기 때문이다. 각 값은 Response 승격(챕터 06·09·10)으로
     * 재기준된 뒤의 것이다.
     */
    @Test
    void modulesAreNotEmpty() {
        assertThat(web.size()).as("web 앱이 비면 noClasses() 전역 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(200);
        assertThat(admin.size()).as("admin 앱이 비면 noClasses() 전역 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(290);
        assertThat(ceo.size()).as("ceo 앱이 비면 noClasses() 전역 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(334);
        assertThat(batch.size()).as("batch 앱이 비면 noClasses() 전역 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(28);
    }

    /**
     * {@code readContractsShouldBeFrameworkFree}의 anchor.
     *
     * <p>이 계약들은 앱 패키지가 아니라 split package인 {@code com.tastyhouse.application}에 있어
     * 위 앱 importer들에 잡히지 않는다. 별도 anchor가 없으면 계약이 통째로 사라져도 규칙이 조용히 통과한다.
     *
     * <p>하한 227은 통합 전 4개 모듈의 합이다(web 85 + admin 79 + ceo 61 + batch 2). 여기서는 계약이
     * 한 트리로 합쳐져 소유 앱을 구분할 수 없으므로 앱별로 쪼개지 않는다 — 실측 271개에 대한
     * 합계 하한으로 대량 소실을 잡는다.
     */
    @Test
    void readContractsExist() {
        assertThat(readContracts.stream()
            .filter(c -> resideInAPackage("..port.out..").test(c))
            .filter(RuleAnchorTest::isOwnedByThisModule)
            .count())
            .as("앱 단독 읽기 계약이 0건이면 프레임워크-프리 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(227);
    }

    /**
     * 이 모듈이 <b>직접 소유</b>한 계약인지 판별한다.
     *
     * <p>이 필터가 없으면 anchor가 공허 통과를 못 막는다. {@code importPackages("com.tastyhouse.application")}은
     * 테스트 런타임 클래스패스 전체를 훑으므로 <b>{@code domain-module}의 공유 계약 55개까지 함께 세어진다</b>
     * — split package라 패키지만으로는 소유 모듈을 가릴 수 없기 때문이다. 그 55개가 하한을 떠받쳐 주면
     * 정작 이 모듈의 계약이 사라져도 anchor가 통과한다.
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
            .filter(uri -> !uri.contains(".jar"))
            .isPresent();
    }

}
