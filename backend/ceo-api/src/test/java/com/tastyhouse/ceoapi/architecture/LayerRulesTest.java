package com.tastyhouse.ceoapi.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.assignableTo;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ceo-api 레이어 경계 규칙(ArchUnit).
 *
 * <p>core-module → domain-module 전환으로 확정된 구조를 컴파일 게이트로 강제한다.
 *
 * <p>전환이 끝나 모든 규칙이 실제 대상 클래스를 갖게 되었으므로 {@code allowEmptyShould(true)}를
 * 제거했다 — 규칙이 대상 0건으로 공허하게 통과하면 그 자체가 실패로 드러나야 한다.
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.ceoapi");

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
     * <p>이 모듈은 현재 위반 0건이므로 예외 목록이 없다(web-api는 {@code ReviewCommandService}
     * 하나를 P3 대기로 제외 중).
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
     * <p>남은 예외 클래스는 write 포트를 표현 목적 조회에 쓰고 있어(= query DAO로 내려야 하는
     * 조회가 write 포트에 남아 있는 상태) 아직 이관 대상이다. 규칙 전체를 끄지 않고 클래스명으로
     * 명시적으로 제외한다. shop 관련 세 QueryService(가게소개·영업시간·휴무)는 P5 이관으로
     * {@code ShopDetailRepository} 주입이 사라져 예외 목록에서 제거했다.
     *
     * <p><strong>간접 위반은 이 규칙으로 잡히지 않는다.</strong> 이 모듈의
     * {@code ShopOwnershipValidator}는 내부에 {@code ShopRepository}(write 포트)를 보유하며, 다수의
     * {@code *QueryService}가 소유권 검증을 위해 그 협력 빈을 주입한다 — 그 경로는 write 포트를
     * <em>직접</em> 주입하지 않으므로 위 예외 목록에 없는 QueryService도 이 규칙을 통과한다.
     * 직접 주입만 규칙화하고, 간접 건은 P5 이관 후 재평가한다.
     */
    // TODO(P5): 아래 예외 클래스들의 write 포트 주입을 infra query DAO로 이관하고 예외 목록을 비운다.
    //           ShopOwnershipValidator 경유 간접 주입도 P5 이관 후 규칙화 가능 여부를 재평가한다.
    @Test
    void queryServicesShouldNotDependOnWritePorts() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("QueryService")
            .and().haveSimpleNameNotEndingWith("CeoQueryService")                // TODO(P5)
            .should().dependOnClassesThat().resideInAnyPackage("com.tastyhouse.domain..repository..")
            .because("QueryService는 write 포트를 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    /**
     * 컨트롤러는 domain-free다. HTTP 경계는 식별자를 {@code Long}, 도메인 enum을 {@code String}으로
     * 받고 승격은 Service가 담당하므로(ID VO·도메인 enum 경계 규칙), 컨트롤러가
     * {@code com.tastyhouse.domain..}를 알 이유가 없다.
     *
     * <p>{@code domain.shared.page.PageResult}는 차단 대상에서 제외한다 — 페이징 컨트롤러가
     * QueryService가 반환한 {@code PageResult<T>}를 해체해 {@code ApiResponse.success(...)} 4-인자
     * 호출에 넘기는 것은 CLAUDE.md가 문서화한 페이징 관행(공용 페이징 <em>계약</em>이지 도메인
     * 모델이 아니다)이다. 클래스 단위가 아니라 타입 단위로 좁혀 다른 도메인 타입 유입은 계속 잡는다.
     *
     * <p>이 모듈은 현재 위반 0건이므로 클래스 예외 목록이 없다(web-api는 {@code PaymentApiController}
     * 하나를 P5 대기로 제외 중).
     */
    @Test
    void controllersShouldBeDomainFree() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat(
                resideInAPackage("com.tastyhouse.domain..")
                    .and(not(assignableTo("com.tastyhouse.domain.shared.page.PageResult")))
                    .as("com.tastyhouse.domain.. (PageResult 제외)"))
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
}
