package com.tastyhouse.adminapi.architecture;

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
 * admin-api 레이어 경계 규칙(ArchUnit).
 *
 * <p>core-module → domain-module 전환으로 확정된 구조를 컴파일 게이트로 강제한다.
 *
 * <p>전환이 끝나 모든 규칙이 실제 대상 클래스를 갖게 되었으므로 {@code allowEmptyShould(true)}를
 * 제거했다 — 규칙이 대상 0건으로 공허하게 통과하면 그 자체가 실패로 드러나야 한다.
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.adminapi");

    /**
     * application 서비스(도메인당 {@code {도메인}CommandService}/{@code {도메인}QueryService} CQRS 분리)는
     * HTTP 계층을 알지 않는다. 컨트롤러가 Request를 원시 필드로 언패킹해 넘기고, Response 조립은
     * 서비스의 private 매퍼가 담당하므로 서비스가 {@code org.springframework.web}·{@code jakarta.servlet}에
     * 의존할 일이 없다.
     *
     * <p>전환 완료로 이 서비스들은 {@code ..application..}이 아니라 도메인 패키지에 직접 놓이므로,
     * 클래스 이름(={@code *CommandService}/{@code *QueryService})으로 대상을 잡는다 — 과거
     * {@code ..application..} 패키지 매칭은 대상 0건으로 공허하게 통과하고 있었다.
     *
     * <p>차단 대상은 요청 바인딩·서블릿·{@code HttpStatus} 등 web <em>플럼빙</em>이다.
     * {@code org.springframework.web.multipart.MultipartFile}은 제외한다 — 파일 업로드 서비스가
     * 업로드 자체를 받는 경계 타입이라 {@code ceo-api}의 이미지 변경/콘텐츠보드 서비스가 정당하게
     * 파라미터로 사용하며, 이를 금지하려면 업로드 흐름 재설계가 필요해 이번 전환 범위를 벗어난다.
     */
    @Test
    void applicationServicesShouldNotDependOnWebLayer() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("CommandService")
            .or().haveSimpleNameEndingWith("QueryService")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework.web.bind..",
                "org.springframework.web.servlet..",
                "org.springframework.http..",
                "jakarta.servlet.."
            );

        rule.check(classes);
    }

    @Test
    void controllersShouldNotDependOnRepositories() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");

        rule.check(classes);
    }

    /**
     * 컨트롤러는 조회 어댑터도 직접 주입하지 않는다. 조회는 {@code *QueryService}가 infra query DAO를
     * 주입해 수행하고 Result → Response 변환까지 담당하므로(CQRS 분리 규칙), 컨트롤러가
     * {@code com.tastyhouse.infrastructure..query..}(DAO·Result·SearchCondition)를 알 이유가 없다.
     *
     * <p>이 규칙이 없으면 <b>구조적 구멍</b>이 남는다 — 위
     * {@code controllersShouldNotDependOnRepositories}는 이름 접미어 {@code *Repository}만 매칭하고
     * DAO는 {@code *QueryDao}라 걸리지 않으며, {@code shouldNotDependOnInfrastructurePersistence}는
     * {@code ..persistence..}만 차단하고 DAO는 {@code ..query..}에 있어 역시 걸리지 않는다. 즉 컨트롤러가
     * DAO를 직접 주입해도 기존 규칙 어디에도 잡히지 않았다. 현재 위반은 0건이며, 이 규칙은 그 상태를
     * 고정한다(기존 규칙은 이중 방어로 그대로 유지).
     */
    @Test
    void controllersShouldNotDependOnQueryDaos() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().resideInAnyPackage("com.tastyhouse.infrastructure..query..")
            .because("컨트롤러는 infra query DAO를 직접 주입하지 않는다(조회는 QueryService 경유)");

        rule.check(classes);
    }

    /**
     * api 모듈은 QueryDSL을 알지 않는다. 조회는 infrastructure-module의 {@code <ctx>/query/} DAO가
     * 캡슐화하며, 이 모듈은 그 DAO와 Result DTO만 주입·import한다.
     */
    @Test
    void shouldNotDependOnQuerydsl() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.querydsl..");

        rule.check(classes);
    }

    /**
     * api 모듈은 infra 중 {@code ..query..}(직접)·{@code ..listener..}(간접)만 허용하며,
     * persistence 어댑터(JpaEntity/Mapper/JpaRepository/RepositoryImpl)에 직접 의존하지 않는다.
     */
    @Test
    void shouldNotDependOnInfrastructurePersistence() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..persistence..");

        rule.check(classes);
    }

    /**
     * CQRS 교차 주입 금지(명령 → 조회). {@code *CommandService}는 infra query DAO·Result
     * ({@code com.tastyhouse.infrastructure..query..})와 같은 모듈의 {@code *QueryService}를
     * 주입하지 않는다 — 명령 경로가 표현용 투영에 결합되면 클래스는 둘로 나뉘었지만 의존 그래프는
     * 여전히 하나로 뭉쳐 있어 CQRS 분리가 이름만 남는다. 명령은 식별자만 반환하고 응답이 필요하면
     * 컨트롤러가 커밋 후 QueryService로 재조회한다.
     *
     * <p>이 모듈은 위반 0건이므로 예외 목록이 없다. web-api도 P3에서 {@code ReviewCommandService} 예외가
     * 해소되어 두 모듈 모두 예외 없이 규칙이 적용된다.
     */
    @Test
    void commandServicesShouldNotDependOnQueryDaos() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("CommandService")
            .should().dependOnClassesThat().resideInAnyPackage("com.tastyhouse.infrastructure..query..")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("QueryService")
            .because("CommandService는 ..query..를 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    /**
     * CQRS 교차 주입 금지(조회 → 쓰기). {@code *QueryService}는 domain-module의 write 포트
     * ({@code com.tastyhouse.domain..repository..})를 주입하지 않는다 — 조회 트랜잭션
     * ({@code readOnly = true})에서 쓰기 경로가 열리는 것을 구조적으로 막는다. 조회는 infra
     * {@code <ctx>/query/} DAO만 쓴다.
     *
     * <p>아래 예외 클래스들은 write 포트를 표현 목적 조회에 쓰고 있어(= query DAO로 내려야 하는
     * 조회가 write 포트에 남아 있는 상태) P5 태스크의 이관 대상이다. 규칙 전체를 끄지 않고
     * 클래스명으로 명시적으로 제외한다.
     */
    // TODO(P5): 아래 예외 클래스들의 write 포트 주입을 infra query DAO로 이관하고 예외 목록을 비운다.
    @Test
    void queryServicesShouldNotDependOnWritePorts() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("QueryService")
            // P5 이관으로 ShopDetailRepository 주입은 사라졌고, 남은 것은 가게 단건 상세
            // (ShopRepository#findById — 도메인 모델을 그대로 응답에 매핑)뿐이다.
            .and().haveSimpleNameNotEndingWith("ShopQueryService")     // TODO(P7)
            .and().haveSimpleNameNotEndingWith("MemberQueryService")   // TODO(P5)
            .and().haveSimpleNameNotEndingWith("AdminQueryService")    // TODO(P5)
            .should().dependOnClassesThat().resideInAnyPackage("com.tastyhouse.domain..repository..")
            .because("QueryService는 write 포트를 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    /**
     * 컨트롤러는 domain-free다. HTTP 경계는 식별자를 {@code Long}, 도메인 enum을 {@code String}으로
     * 받고 승격은 Service가 담당하므로(ID VO·도메인 enum 경계 규칙), 컨트롤러가
     * {@code com.tastyhouse.domain..}를 알 이유가 없다.
     *
     * <p>예외·제외 항목은 없다. 페이징 컨트롤러도 QueryService가 돌려주는
     * {@code PaginationResponse<T>}(api-common-module)를 해체해 {@code ApiResponse.success(...)}
     * 4-인자 호출에 넘기므로 {@code domain.shared.page.PageResult}를 알 필요가 없다.
     */
    @Test
    void controllersShouldBeDomainFree() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().resideInAnyPackage("com.tastyhouse.domain..")
            .because("컨트롤러는 com.tastyhouse.domain..를 import하지 않는다(HTTP 경계는 Long·String)");

        rule.check(classes);
    }

    /**
     * Request/Response record는 domain-free·infra-free 순수 데이터 홀더다(검증 + Swagger 스키마).
     * result 객체를 통째로 받지 않고 원시타입으로 낱개 언패킹해 받으므로 infra query result 구조를
     * 알 필요가 없고, 도메인 enum·VO 승격은 Service가 담당한다.
     *
     * <p>CLAUDE.md에 문서화된 {@code PaginationResponse}의 {@code PageResult} 예외는 이 규칙의
     * 대상이 아니다 — 그 타입은 {@code ..response..}가 아니라 {@code common/}에 있어 매칭되지 않는다.
     */
    @Test
    void requestResponseRecordsShouldBeDomainAndInfraFree() {
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
     * 컨트롤러는 인바운드 포트(UseCase 인터페이스)만 주입하고 {@code *CommandService} 구체 클래스를
     * 알지 않는다. 이 규칙이 완전 매핑 전환의 <b>컴파일 게이트</b>다 — 인터페이스를 만들어 두어도
     * 컨트롤러가 구체 클래스를 계속 주입하면 경계는 이름만 남는다.
     *
     * <p>{@code *QueryService} 구체 주입 금지는 챕터 03에서 추가한다. 이 챕터 동안 컨트롤러는
     * QueryService를 구체 클래스로 계속 주입하므로, 지금 함께 막으면 전 컨트롤러가 위반이 된다.
     */
    @Test
    void controllersShouldDependOnUseCasesOnly() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("CommandService")
            .because("컨트롤러는 CommandUseCase 인터페이스만 주입한다(구체 클래스 금지)");

        rule.check(classes);
    }

    /**
     * {@code *CommandService}는 인바운드 포트를 최소 1개 구현한다. 위
     * {@code controllersShouldDependOnUseCasesOnly}의 짝으로, 그 규칙만 있으면 컨트롤러가 구체 클래스를
     * 안 볼 뿐 서비스가 아무 인터페이스도 구현하지 않는 상태(=포트 없이 빈 주입만 우회)가 통과한다.
     */
    @Test
    void commandServicesShouldImplementUseCase() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("CommandService")
            .should().implement(resideInAPackage("..application.port.in.."))
            .because("CommandService는 대응 CommandUseCase를 구현한다");

        rule.check(classes);
    }

    /**
     * Command record는 경계 타입만 싣는다 — 식별자는 {@code Long}, 도메인 enum 후보는 {@code String}으로
     * 받고 승격({@code XxxId.of}·{@code Enum.from})은 서비스 내부에서 한다.
     *
     * <p>차단 대상은 도메인 <em>모델</em>이다(완전 매핑 그림 8.3에서 애그리거트는 Command와 다른 계층
     * 칸에 있다). {@code com.tastyhouse.domain.exception..}은 예외로 허용한다 — {@code BusinessException}·
     * {@code ErrorCode}는 애그리거트가 아니라 전 계층이 공유하는 <b>횡단 관심사(에러 계약)</b>이고,
     * compact constructor의 구조적 가드가 이를 던져야 응답 코드가 나머지 경로와 같은 형태로 나간다.
     * 이 carve-out이 없으면 §2가 요구하는 가드를 아예 쓸 수 없다.
     *
     * <p>{@code org.springframework.web.multipart..}({@code MultipartFile})도 제외한다 — 이 규칙이
     * 금지하려는 것은 <b>Command record의 필드</b>로 업로드 타입을 싣는 것인데, ArchUnit의 의존 그래프는
     * 같은 패키지에 있는 <b>UseCase 인터페이스의 메서드 파라미터</b>까지 함께 잡는다. 업로드를 받는 연산은
     * {@code method(XxxCommand, MultipartFile)}처럼 별도 파라미터로 두는 것이 규정된 형태이므로(§6),
     * 그것까지 막으면 업로드 흐름을 재설계해야 한다. Command 필드로 실리는 것은 아래
     * {@code commandRecordsShouldNotHoldMultipartFile}이 따로 막는다.
     */
    @Test
    void commandRecordsShouldBeBoundaryTyped() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application.port.in..")
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "com.tastyhouse.domain..",
                    "com.tastyhouse.infrastructure..",
                    "org.springframework.web.."
                ).and(not(resideInAPackage("com.tastyhouse.domain.exception..")))
                 .and(not(resideInAPackage("org.springframework.web.multipart..")))
            )
            .because("Command는 도메인 모델·infra·web 타입을 싣지 않는다(에러 계약은 횡단 관심사라 예외)");

        rule.check(classes);
    }

    /**
     * 인바운드 포트는 web 플럼빙을 알지 않는다. {@code ..port.in..}이 {@code MultipartFile} 외의
     * 요청 바인딩·서블릿·{@code HttpStatus}에 의존하면 application 계층이 HTTP에 묶여, 포트를 도입한
     * 목적(경계 계약을 전송 방식과 분리)이 사라진다.
     *
     * <p>{@code MultipartFile}은 {@code org.springframework.web.multipart..}라 위
     * {@code commandRecordsShouldBeBoundaryTyped}가 이미 Command 필드로는 막는다. UseCase 메서드의
     * <em>별도 파라미터</em> 사용은 업로드 경계 타입으로 존치한다(§6).
     */
    @Test
    void portInShouldNotDependOnWebPlumbing() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application.port.in..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "org.springframework.web.bind..",
                "org.springframework.web.servlet..",
                "org.springframework.http..",
                "jakarta.servlet.."
            )
            .because("인바운드 포트는 HTTP 전송 방식을 알지 않는다");

        rule.check(classes);
    }

    /**
     * {@code *CommandService}는 {@code ..request..}를 알지 않는다. 컨트롤러가 Request를 Command로
     * 매핑해 넘기므로(완전 매핑 — 매핑은 인바운드 어댑터 소유) 서비스가 HTTP 요청 DTO를 볼 이유가 없다.
     *
     * <p>이 모듈은 봉인 예외가 없다 — 전 {@code *CommandService}가 규칙 대상이다.
     */
    @Test
    void commandServicesShouldNotDependOnRequestRecords() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("CommandService")
            .should().dependOnClassesThat().resideInAnyPackage("..request..")
            .because("CommandService는 Request record를 받지 않는다(매핑은 컨트롤러가 소유)");

        rule.check(classes);
    }
}
