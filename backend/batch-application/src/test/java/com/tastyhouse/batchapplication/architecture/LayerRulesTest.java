package com.tastyhouse.batchapplication.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * batch-application 레이어 경계 규칙(ArchUnit).
 *
 * <p>챕터 01로 batch의 application 계층({@code <job>/{port/in,service}} · {@code crawling/bbq} ·
 * {@code exception})이 batch-module에서 이 모듈로 물리 분리됐다. batch-module에 있던 규칙 중
 * application 계층을 대상으로 하던 것들이 여기로 따라왔고, 물리 분리로 비로소 표현 가능해진
 * 규칙 2개({@code applicationMustBeServletFree} · {@code applicationMustNotDependOnAdapters})를 신설했다.
 *
 * <p>infra·QueryDSL 차단은 이제 build.gradle이 1차로 막는다(이 모듈은 infrastructure-module을
 * 컴파일 클래스패스에 두지 않는다). 그럼에도 규칙을 남기는 이유는 회귀 방어다 — 누군가
 * build.gradle에 의존 한 줄을 되돌리면 컴파일은 통과하고 계층만 조용히 무너진다.
 *
 * <p>{@code allowEmptyShould(true)}는 이 파일 어디에도 쓰지 않는다. 규칙이 대상을 잃으면
 * 공허하게 통과시키지 말고 규칙을 지우거나 anchor를 고친다.
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.batchapplication");

    /**
     * 챕터 03으로 이 모듈이 소유하게 된 batch 단독 읽기 계약(split package).
     *
     * <p>위 {@code classes}와 분리하는 이유는, 기존 규칙들이 {@code com.tastyhouse.application..port.out..}을
     * <b>외부</b>로 취급하기 때문이다. 한 importer에 합치면 계약 자신이 그 규칙의 대상이 되어 의미가 뒤집힌다.
     */
    private final JavaClasses readContracts = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    /**
     * 잡 서비스는 HTTP 계층을 알지 않는다.
     *
     * <p>batch-module에서 이동한 규칙이다. batch는 CQRS 분리를 쓰지 않아
     * {@code *CommandService}/{@code *QueryService}가 0개이고 잡 본문을 {@code *SchedulerService}에
     * 담으므로, 세 이름을 모두 대상으로 잡아 실재하는 잡 서비스(Grade/Rank/Product/AdminDong/
     * SearchKeyword/ReviewBlind/ProductSoldOutRelease)에 anchor 한다.
     *
     * <p>아래 {@code applicationMustBeServletFree}가 모듈 전체를 더 넓게 막지만, 이 규칙은
     * {@code org.springframework.web.bind..}·{@code org.springframework.http..}처럼 서비스가 특히
     * 끌어들이기 쉬운 요청 바인딩 플럼빙을 서비스 이름에 직접 묶어 둔다.
     */
    @Test
    void applicationServicesShouldNotDependOnWebLayer() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("CommandService")
            .or().haveSimpleNameEndingWith("QueryService")
            .or().haveSimpleNameEndingWith("SchedulerService")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework.web.bind..",
                "org.springframework.web.servlet..",
                "org.springframework.http..",
                "jakarta.servlet.."
            );

        rule.check(classes);
    }

    /**
     * 인바운드 포트({@code ..port.in..})는 경계 타입만 노출한다.
     *
     * <p>batch-module에서 이동한 규칙이다. 배치 잡은 스케줄이 유일한 입력이라 Command record를 두지
     * 않으므로 대상은 UseCase 인터페이스 7종이고, 이들이 도메인 모델·infra·web 타입을 시그니처에
     * 드러내지 않아야 스케줄러(adapter)가 그 타입들을 함께 보지 않게 된다.
     *
     * <p>{@code com.tastyhouse.domain.exception}은 carve-out 으로 허용한다 — 예외는 횡단 관심사라
     * 계층 칸이 없다(기존 api 모듈 규칙과 동일한 판단을 유지한다).
     *
     * <p><b>주의 — 지금은 검사할 표면이 없다.</b> 배치 잡은 입력이 없어 UseCase 7개가 전부
     * 파라미터·반환값 없는 {@code void foo()} 하나뿐이라, 의존 그래프에 잡힐 타입 자체가 0건이다
     * ({@code RuleAnchorTest}가 세는 것은 "인터페이스가 존재함"이지 "검사 대상이 있음"이 아니다).
     * 규칙과 carve-out은 <b>UseCase가 처음으로 파라미터를 갖는 시점</b>을 위해 미리 세워 둔 것이므로,
     * 지금 아무것도 걸리지 않는다는 이유로 carve-out을 죽은 코드로 보고 지우지 말 것.
     */
    @Test
    void inboundPortsShouldBeBoundaryTyped() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..port.in..")
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "com.tastyhouse.domain..",
                    "com.tastyhouse.infrastructure..",
                    "org.springframework.web.."
                ).and(not(resideInAPackage("com.tastyhouse.domain.exception..")))
            )
            .because("인바운드 포트는 도메인 모델·infra·web 타입을 경계 밖으로 노출하지 않는다"
                + "(domain.exception은 횡단 관심사라 carve-out)");

        rule.check(classes);
    }

    /**
     * 이 모듈은 QueryDSL을 알지 않는다. 조회는 infrastructure-module의 {@code <ctx>/query/} DAO가
     * 캡슐화하며, 이 모듈은 {@code com.tastyhouse.application..port.out}의 읽기 포트와 Result DTO만 주입·import 한다.
     *
     * <p><b>현재 휴면 상태다</b> — QueryDSL이 클래스패스에 없어 위반 코드는 컴파일되지 않는다.
     * build.gradle에 의존이 추가되는 회귀에 대비한 방어선으로 유지한다.
     */
    @Test
    void shouldNotDependOnQuerydsl() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.querydsl..");

        rule.check(classes);
    }

    /**
     * 이 모듈은 infrastructure-module을 알지 않는다.
     *
     * <p>batch-module의 {@code shouldNotDependOnInfrastructurePersistence}를 이동하면서
     * {@code ..persistence..}가 아니라 <b>infra 전체</b>로 범위를 넓혔다 — 모듈 분리의 핵심이
     * "application은 infra를 아예 모른다"이고, build.gradle이 이미 그 상태를 만들어 두었으므로
     * 좁은 규칙을 유지할 이유가 없다.
     *
     * <p><b>현재 휴면 상태다</b>(위와 같은 이유 — infra가 클래스패스에 없다). 이 모듈에서 계층을
     * 실제로 강제하는 것은 이 규칙이 아니라 <b>build.gradle</b>이며, 규칙은 그 한 줄이 되돌려지는
     * 회귀를 잡는 2차 방어선이다.
     */
    @Test
    void shouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure..");

        rule.check(classes);
    }

    /**
     * Response record는 domain-free·infra-free 순수 데이터 홀더다.
     *
     * <p>batch-module에서 {@code crawling/bbq/response/}와 함께 이동한 규칙이다. 대상은 record 4종
     * ({@code BbqProductResponse}·{@code BbqProductCategoryResponse}·
     * {@code BbqProductSubOptionResponse}·{@code SubOptionItemDetailResponse})으로 공허하지 않다.
     */
    @Test
    void responseRecordsShouldBeDomainAndInfraFree() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..request..", "..response..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.tastyhouse.domain..",
                "com.tastyhouse.infrastructure.."
            )
            .because("Request/Response record는 domain-free·infra-free 순수 데이터 홀더다");

        rule.check(classes);
    }

    /**
     * <b>신설</b> — 모듈 전체가 서블릿·web MVC를 알지 않는다.
     *
     * <p>batch에는 HTTP 경계도 파일 업로드도 없으므로 api 모듈이 달고 있는
     * {@code MultipartFile} 예외가 필요 없다. 즉 이 모듈에서는 예외 없는 완전한 servlet-free를
     * 표현할 수 있고, 이것이 application 계층을 물리 분리해서 얻는 것 중 하나다.
     *
     * <p>위 {@code applicationServicesShouldNotDependOnWebLayer}가 서비스 이름에 anchor 한 좁은 규칙이라면
     * 이 규칙은 record·executor·config를 포함한 모듈 전체를 덮는다(좁은 쪽은 사실상 이 규칙에 포섭되며,
     * {@code org.springframework.http..} 하나만 추가로 막는다).
     *
     * <p><b>현재 휴면 상태다</b> — spring-web·jakarta.servlet이 클래스패스에 없다. 회귀 방어선이다.
     */
    @Test
    void applicationMustBeServletFree() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                "jakarta.servlet..",
                "org.springframework.web.."
            )
            .because("batch application 계층에는 HTTP 경계가 없다(업로드가 없어 MultipartFile 예외도 불필요)");

        rule.check(classes);
    }

    /**
     * <b>신설</b> — application은 adapter를 역참조하지 않는다.
     *
     * <p>의존 방향은 {@code batch-module(adapter) → batch-application(application) → domain} 한 방향이다.
     * 스케줄러가 잡 UseCase를 주입하는 것은 정방향이고, 그 반대(서비스가
     * {@code com.tastyhouse.batch..}의 무언가를 import)는 모듈 신설의 전제를 깨뜨린다.
     *
     * <p><b>이 규칙은 현재 휴면(dormant) 상태다</b> — build.gradle이 batch-module 의존을 두지 않아
     * {@code com.tastyhouse.batch..}가 클래스패스에 아예 없고, 따라서 위반하는 코드는 컴파일되지 않는다.
     * 순환을 실수로 추가하는 경우도 이 규칙이 잡지 못한다 — Gradle이 <em>configuration 단계</em>에서
     * 순환 의존을 명시적 메시지로 거부하므로 테스트 컴파일에 도달하지 못한다.
     *
     * <p>그럼에도 남기는 이유는 <b>의존 방향을 코드로 선언해 두기 위해서</b>다. 누군가
     * batch-application에 batch-module 의존을 (순환이 아닌 형태로) 추가하면 그 순간 이 규칙이 발화한다.
     */
    @Test
    void applicationMustNotDependOnAdapters() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.batch..")
            .because("의존 방향은 batch-module(adapter) → batch-application → domain 한 방향이다");

        rule.check(classes);
    }

    /**
     * {@code *SchedulerService}는 인바운드 포트를 최소 1개 구현한다.
     *
     * <p>batch-module에서 이동하면서 패키지 패턴을 {@code ..application.port.in..} →
     * {@code ..port.in..}으로 갱신했다(이동으로 중간 {@code application} 세그먼트가 사라졌다).
     */
    @Test
    void schedulerServicesShouldImplementUseCase() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("SchedulerService")
            .should().implement(resideInAPackage("..port.in.."))
            .because("SchedulerService는 대응 잡 UseCase를 구현한다");

        rule.check(classes);
    }

    /**
     * 앱 간 수평 의존을 금지한다.
     *
     * <p>application-common-module 해체로 읽기 계약이 앱별 모듈로 분산되면서, 한 앱이 다른 앱의
     * 계약을 참조하려는 유혹이 생긴다. 공유 계약은 domain-module이 소유하므로(챕터 05) 앱 패키지를
     * 참조할 정당한 이유가 없다 — 읽기 계약은 com.tastyhouse.application.. 에 있고 이 규칙의
     * 대상이 아니다.
     */
    @Test
    void shouldNotDependOnOtherApplicationModules() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.tastyhouse.webapplication..",
                "com.tastyhouse.adminapplication..",
                "com.tastyhouse.ceoapplication.."
            )
            .because("앱 간 수평 의존을 두지 않는다 — 공유 읽기 계약은 domain-module이 소유한다");

        rule.check(classes);
    }

    /**
     * 읽기 계약은 프레임워크-프리를 유지한다(챕터 09).
     *
     * <p>이 계약들은 원래 {@code application-common-module}에 있었고, 그 모듈이 루트 build.gradle의
     * spring-boot-starter 일괄 적용에서 <b>제외</b>돼 있어 {@code org.springframework} import가 아예
     * 컴파일 에러였다 — 프레임워크-프리가 빌드 게이트로 강제되던 것이다. 챕터 03으로 이 모듈에
     * 들어오면서 그 게이트가 사라졌다(이 모듈은 starter를 받는다). 잃어버린 강제를 규칙으로 되살린다.
     *
     * <p>허용 대상을 {@code java..}·{@code com.tastyhouse.domain..}과 자기 자신으로 한정한다. 계약이
     * 참조해도 되는 것은 도메인 타입뿐이며, 이는 build.gradle을 바꾸지 않고도 계약을 옮길 수 있었던
     * 근거이기도 하다.
     */
    @Test
    void readContractsShouldBeFrameworkFree() {
        ArchRule rule = classes()
            .that().resideInAPackage("com.tastyhouse.application..port.out..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "java..",
                "com.tastyhouse.domain..",
                "com.tastyhouse.application..port.out.."
            )
            .because("읽기 계약은 도메인 타입만 참조한다 — application-common-module에서 "
                + "빌드 게이트로 강제되던 프레임워크-프리를 규칙으로 승계한다");

        rule.check(readContracts);
    }

}
