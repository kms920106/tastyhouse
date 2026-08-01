package com.tastyhouse.webapi.architecture;

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
 * web-api 레이어 경계 규칙(ArchUnit).
 *
 * <p>core-module → domain-module 전환으로 확정된 구조를 컴파일 게이트로 강제한다.
 *
 * <p>전환이 끝나 모든 규칙이 실제 대상 클래스를 갖게 되었으므로 {@code allowEmptyShould(true)}를
 * 제거했다 — 규칙이 대상 0건으로 공허하게 통과하면 그 자체가 실패로 드러나야 한다.
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.webapi");

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
     * <p>예외 {@code ReviewCommandService}: 리뷰 작성 시 상품 스냅샷·회원 검증에 조회를 직접 쓰고 있다.
     * 해소는 P3 태스크 담당이며, 규칙 전체를 끄지 않고 이 클래스 하나만 명시적으로 제외한다.
     */
    // TODO(P3): ReviewCommandService의 MemberQueryDao·ProductQueryService 직접 주입을 제거하고
    //           아래 예외 항목을 삭제한다.
    @Test
    void commandServicesShouldNotDependOnQueryDaos() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("CommandService")
            .and().haveSimpleNameNotEndingWith("ReviewCommandService")
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
     *
     * <p>{@code ceo-api}의 {@code ShopOwnershipValidator}처럼 내부에 write 포트를 보유한 협력 빈을
     * QueryService가 주입하는 <em>간접</em> 위반은 이 규칙으로 잡히지 않는다. 직접 주입만 규칙화하고,
     * 간접 건은 P5 이관 후 재평가한다.
     */
    // TODO(P5): 아래 예외 클래스들의 write 포트 주입을 infra query DAO로 이관하고 예외 목록을 비운다.
    @Test
    void queryServicesShouldNotDependOnWritePorts() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("QueryService")
            .and().haveSimpleNameNotEndingWith("ShopQueryService")      // TODO(P5)
            .and().haveSimpleNameNotEndingWith("ReviewQueryService")    // TODO(P5)
            .and().haveSimpleNameNotEndingWith("FollowQueryService")    // TODO(P5)
            .and().haveSimpleNameNotEndingWith("MemberQueryService")    // TODO(P5)
            .should().dependOnClassesThat().resideInAnyPackage("com.tastyhouse.domain..repository..")
            .because("QueryService는 write 포트를 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    /**
     * 컨트롤러는 domain-free다. HTTP 경계는 식별자를 {@code Long}, 도메인 enum을 {@code String}으로
     * 받고 승격은 Service가 담당하므로(ID VO·도메인 enum 경계 규칙), 컨트롤러가
     * {@code com.tastyhouse.domain..}를 알 이유가 없다. 컨트롤러가 도메인 enum을 직접 노출하면
     * API 계약이 도메인 모델에 결합되어 enum 상수 추가가 곧 공개 스키마 변경이 된다.
     *
     * <p>{@code domain.shared.page.PageResult}는 <em>차단 대상에서 제외</em>한다. 조사 결과 페이징
     * 컨트롤러 5개({@code Event}/{@code Follow}/{@code MemberMe}/{@code Search}/{@code Shop})가
     * QueryService가 반환한 {@code PageResult<T>}를 받아 {@code content()}/{@code page()}/
     * {@code size()}/{@code totalElements()}로 해체해 {@code ApiResponse.success(...)} 4-인자 호출에
     * 넘긴다 — 이는 CLAUDE.md가 문서화한 페이징 관행(공용 페이징 <em>계약</em>이지 도메인 모델이
     * 아니다)이므로 위반이 아니다. 클래스 단위 제외가 아니라 <em>타입 단위</em> 제외로 좁혀
     * 다른 도메인 타입 유입은 계속 잡히게 한다.
     *
     * <p>예외 {@code PaymentApiController}: 결제 취소 코드({@code PaymentCancelCode})를 도메인 enum
     * 그대로 받고 있다(도메인 enum 경계 규칙 위반 — {@code String}으로 받아 Service에서 승격해야
     * 한다). 해소는 P5 범위의 경계 타입 정리 대상이며, 규칙 전체를 끄지 않고 이 클래스 하나만
     * 명시적으로 제외한다.
     */
    // TODO(P5): PaymentApiController의 PaymentCancelCode 직접 의존을 String 경계로 바꾸고
    //           아래 예외 항목을 삭제한다.
    @Test
    void controllersShouldBeDomainFree() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .and().haveSimpleNameNotEndingWith("PaymentApiController")
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
