package com.tastyhouse.ceoapi.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * ceo-api 레이어 경계 규칙(ArchUnit).
 *
 * <p><b>챕터 04 개정</b> — application 계층이 ceo-application 모듈로 물리 분리되면서 이 파일은
 * <b>인바운드 어댑터(잔류 계층)의 규칙만</b> 담는다. application 계층을 대상으로 하던 규칙
 * ({@code commandServicesShouldNotDependOnQueryDaos} · {@code queryServicesShouldNotDependOnWritePorts} ·
 * {@code *ShouldImplementUseCase} · {@code commandRecords*} · {@code portIn*} ·
 * {@code commandServicesShouldNotDependOnRequestRecords} · {@code applicationServicesShouldNotDependOnWebLayer})
 * 은 전부 {@code ceo-application}의 동명 테스트로 이동했다 — 이 모듈에 남겨 두면 대상 0건으로
 * <b>공허하게 통과</b>하기 때문이다.
 *
 * <p>잔류 규칙 중 application을 가리키던 패턴은 신규 패키지 기준으로 갱신했다
 * ({@code ..application.service..} → {@code com.tastyhouse.ceoapplication..service..}).
 *
 * <p>web-api에 있던 {@code shouldDependOnOauthSpiOnlyNotProviderPackages}는 이 모듈에 두지 않는다 —
 * ceo에는 소셜 로그인이 없어 대상 0건으로 <b>공허하게 통과</b>하기 때문이다(전환 전 ceo-api
 * LayerRulesTest에도 없던 규칙이다).
 *
 * <p>{@code allowEmptyShould(true)}는 이 파일 어디에도 쓰지 않는다.
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.ceoapi");

    @Test
    void controllersShouldNotDependOnRepositories() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");

        rule.check(classes);
    }

    /**
     * 컨트롤러는 조회 어댑터도 직접 주입하지 않는다. 조회는 {@code *QueryService}가 읽기 포트를
     * 주입해 수행하고 Result → Response 변환까지 담당하므로(CQRS 분리 규칙), 컨트롤러가
     * 읽기 포트({@code com.tastyhouse.application..port.out..})를 알 이유가 없다.
     */
    @Test
    void controllersShouldNotDependOnQueryDaos() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().resideInAPackage(
                "com.tastyhouse.application..port.out..")
            .because("컨트롤러는 조회 어댑터도 읽기 포트도 직접 주입하지 않는다(조회는 QueryService 경유)");

        rule.check(classes);
    }

    /**
     * api 모듈은 QueryDSL을 알지 않는다.
     */
    @Test
    void shouldNotDependOnQuerydsl() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.querydsl..");

        rule.check(classes);
    }

    /**
     * persistence 어댑터(JpaEntity/Mapper/JpaRepository/RepositoryImpl)에 직접 의존하지 않는다.
     */
    @Test
    void shouldNotDependOnInfrastructurePersistence() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..persistence..");

        rule.check(classes);
    }

    /**
     * 컨트롤러는 domain-free다. HTTP 경계는 식별자를 {@code Long}, 도메인 enum을 {@code String}으로
     * 받고 승격은 Service가 담당하므로, 컨트롤러가 {@code com.tastyhouse.domain..}를 알 이유가 없다.
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
     * Request record는 domain-free·infra-free 순수 데이터 홀더다(검증 + Swagger 스키마).
     *
     * <p><b>챕터 04 개정</b> — {@code response/}는 QueryService가 조립하는 표현 계약이라
     * ceo-application으로 함께 이동했으므로, 이 모듈에 남은 대상은 {@code ..request..}뿐이다.
     * Response 쪽 동일 규칙은 ceo-application이 담당한다.
     */
    @Test
    void requestRecordsShouldBeDomainAndInfraFree() {
        ArchRule rule = noClasses()
            .that().resideInAnyPackage("..request..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.tastyhouse.domain..",
                "com.tastyhouse.infrastructure.."
            )
            .because("Request record는 domain-free·infra-free 순수 데이터 홀더다");

        rule.check(classes);
    }

    /**
     * 컨트롤러는 인바운드 포트(UseCase 인터페이스)만 주입하고 구체 서비스를 알지 않는다.
     *
     * <p><b>챕터 04 개정</b> — 구체 서비스가 ceo-application으로 이동했으므로 이제 이 규칙 위반은
     * 애초에 컴파일되지 않는다(그 모듈의 {@code service} 패키지를 import해야 하므로). 그럼에도
     * 접미어 기준으로 남겨 두는 이유는, 누군가 구체 서비스를 ceo-api로 되돌리는 시도가 곧바로
     * 드러나게 하기 위해서다.
     */
    @Test
    void controllersShouldDependOnUseCasesOnly() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("CommandService")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("QueryService")
            .because("컨트롤러는 UseCase 인터페이스만 주입한다(구체 서비스 금지)");

        rule.check(classes);
    }

    /**
     * 인바운드 어댑터는 application 서비스 구체 클래스에 의존하지 않는다 — UseCase 인터페이스만 주입한다.
     *
     * <p><b>챕터 04 개정</b> — 대상 패키지를 {@code ..application.service..}에서
     * {@code com.tastyhouse.ceoapplication..service..}로 갱신했다. 접미어가 아니라 <b>위치</b>로 잡으므로
     * {@code *Service} 같은 비표준 접미어 파사드까지 걸린다 — 접미어 규칙
     * ({@code controllersShouldDependOnUseCasesOnly})만으로는 그물을 빠져나가는 형태이며, web-api·
     * admin-api에서 실제로 파사드 직접 주입을 잡아낸 규칙이다.
     */
    @Test
    void webAdaptersShouldNotDependOnApplicationServices() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..adapter.in.web..")
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.ceoapplication..service..")
            .because("인바운드 어댑터는 UseCase 인터페이스만 주입한다(구체 서비스 금지)");

        rule.check(classes);
    }

    /**
     * <b>신설</b> — api 모듈에는 application 계층이 남아 있지 않다.
     *
     * <p>챕터 04의 물리 분리가 <b>되돌려지지 않았음</b>을 고정하는 규칙이다. 위 규칙들은 "컨트롤러가
     * 무엇을 주입하는가"를 보지만, 누군가 ceo-api 안에 {@code @Service} 빈을 새로 만들어 application
     * 로직을 되살리는 것은 잡지 못한다.
     *
     * <p>{@code @RestController}가 {@code ..adapter.in.web..}에만 존재한다는 짝 규칙
     * ({@link #restControllersShouldResideInWebAdapterPackage})과 함께 3층 구조를 지킨다. 컨트롤러
     * 실존에 anchor 하므로(그 규칙이 {@code classes()} 형태라 대상 0건이면 실패) 공허하지 않다.
     */
    @Test
    void apiModuleMustNotContainApplicationLayer() {
        ArchRule rule = noClasses()
            .should().beAnnotatedWith("org.springframework.stereotype.Service")
            .because("application 계층은 ceo-application 모듈이 소유한다(ceo-api에 @Service 금지)");

        rule.check(classes);
    }

    /**
     * <b>신설</b> — {@code @RestController}는 {@code ..adapter.in.web..}에만 존재한다.
     *
     * <p>위 {@code apiModuleMustNotContainApplicationLayer}의 짝이며, 이 규칙이 {@code classes()}
     * 형태라 <b>컨트롤러 실존에 anchor</b> 한다 — 컨트롤러가 0건이 되면 곧바로 실패한다.
     */
    @Test
    void restControllersShouldResideInWebAdapterPackage() {
        ArchRule rule = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .should().resideInAPackage("..adapter.in.web..")
            .because("컨트롤러는 인바운드 어댑터 패키지에만 둔다(3층 구조)");

        rule.check(classes);
    }

}
