package com.tastyhouse.batch.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * batch-module 레이어 경계 규칙(ArchUnit).
 *
 * <p>core-module → domain-module 전환으로 확정된 구조를 컴파일 게이트로 강제한다. batch는 HTTP
 * 컨트롤러가 없어 web/admin/ceo의 {@code controllersShouldNotDependOnRepositories} 규칙은 두지 않고,
 * api 모듈 공통의 QueryDSL·persistence 차단 규칙을 적용한다.
 *
 * <p>전환이 끝나 모든 규칙이 실제 대상 클래스를 갖게 되었으므로 {@code allowEmptyShould(true)}를
 * 제거했다 — 규칙이 대상 0건으로 공허하게 통과하면 그 자체가 실패로 드러나야 한다.
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.batch");

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
            )
            // batch-module은 CQRS application 서비스를 두지 않는다(스케줄러가 도메인 서비스를 직접 호출).
            // 대상 0건이 정상이므로 공허 통과를 허용한다 — 다른 3개 api 모듈에서는 허용하지 않는다.
            .allowEmptyShould(true);

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
     * Request/Response record는 domain-free·infra-free 순수 데이터 홀더다. 이 모듈에는 HTTP 경계가
     * 없지만 BBQ 크롤링 응답 매핑용 {@code crawling/bbq/response/} record들이 있어 규칙 대상이 되며,
     * 외부 API 응답 DTO가 도메인·infra 타입을 알 이유가 없다는 점은 동일하게 적용된다.
     *
     * <p>web/admin/ceo에 추가한 나머지 CQRS 게이트 3개
     * ({@code commandServicesShouldNotDependOnQueryDaos},
     * {@code queryServicesShouldNotDependOnWritePorts}, {@code controllersShouldBeDomainFree})는
     * 이 모듈에 <strong>두지 않는다</strong> — batch는 CQRS application 서비스도 HTTP 컨트롤러도 두지
     * 않아(스케줄러가 도메인 서비스를 직접 호출) 대상이 구조적으로 0건이고, 그런 규칙을 추가하면
     * {@code allowEmptyShould(true)}로 공허 통과를 열어야 한다. 규칙을 두지 않는 것이 공허하게
     * 통과시키는 것보다 정직하다. batch에 CQRS 서비스나 컨트롤러가 생기면 그 시점에 추가한다.
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
