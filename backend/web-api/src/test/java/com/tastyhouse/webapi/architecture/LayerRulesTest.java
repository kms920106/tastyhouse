package com.tastyhouse.webapi.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.architecture.AppOwnership;
import com.tastyhouse.application.shared.marker.WebApp;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * web-api 레이어 경계 규칙(ArchUnit).
 *
 * <p><b>챕터 02 개정</b> — application 계층이 web-application 모듈로 물리 분리되면서 이 파일은
 * <b>인바운드 어댑터(잔류 계층)의 규칙만</b> 담는다. application 계층을 대상으로 하던 규칙
 * ({@code commandServicesShouldNotDependOnQueryDaos} · {@code queryServicesShouldNotDependOnWritePorts} ·
 * {@code *ShouldImplementUseCase} · {@code commandRecords*} · {@code portIn*} ·
 * {@code commandServicesShouldNotDependOnRequestRecords} · {@code applicationServicesShouldNotDependOnWebLayer})
 * 은 전부 {@code web-application}의 동명 테스트로 이동했다 — 이 모듈에 남겨 두면 대상 0건으로
 * <b>공허하게 통과</b>하기 때문이다.
 *
 * <p>잔류 규칙 중 application을 가리키던 패턴은 신규 패키지 기준으로 갱신했다
 * ({@code ..application.service..} → {@code com.tastyhouse.application..service..}).
 *
 * <p>{@code allowEmptyShould(true)}는 이 파일 어디에도 쓰지 않는다.
 */
class LayerRulesTest {

    private static final String DOMAIN_ROOT = "com.tastyhouse.domain";

    /**
     * api 모듈이 도메인 enum에 호출할 수 있는 읽기 전용 accessor.
     *
     * <p>바이트코드 그래프 실측에서 도출했다(admin-api 기준 {@code name} 57 · {@code getDescription} 8 ·
     * {@code getDisplayName} 1이 전부이고 {@code ordinal}·{@code toString}·{@code values}는 0건).
     * <b>항목을 추가하지 않는다</b> — 이 목록이 커지는 것은 api 모듈이 도메인 로직을 수행하기 시작했다는
     * 신호이므로, 목록을 늘리지 말고 그 호출을 application으로 옮긴다.
     */
    private static final Set<String> ALLOWED_DOMAIN_ENUM_ACCESSORS =
        Set.of("name", "getDescription", "getDisplayName");

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.webapi");

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
     * 읽기 포트를 알 이유가 없다.
     *
     * <p><b>챕터 03 개정 — 판정 기준을 패키지에서 이름으로 바꿨다.</b> 평탄화로
     * {@code <ctx>/port/out}이 "이 도메인의 모든 아웃바운드 계약"을 담게 되면서, 읽기 포트와 함께
     * <b>CommandService가 반환하는 Result record</b>가 같은 패키지에 살게 됐다. 컨트롤러는 그 Result를
     * 정당하게 받아 Response로 조립하므로({@code XxxResponse.from(XxxResult)} — 챕터 06·09·10),
     * {@code ..port.out..} 패키지 술어를 그대로 두면 정상 경로가 전부 위반으로 잡힌다. 이 규칙이
     * 실제로 막으려는 것은 <b>포트 인터페이스의 직접 주입</b>이므로 {@code *QueryPort} 이름으로 잡는다
     * (application 모듈의 {@code commandServicesShouldNotDependOnQueryDaos}와 같은 전환).
     */
    @Test
    void controllersShouldNotDependOnQueryDaos() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("QueryPort")
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
     *
     * <p><b>챕터 06 — {@code domain.shared.page..} carve-out</b>: application이 {@code PageResult}를
     * 반환하고 컨트롤러가 {@code PaginationResponse.from(...)}으로 조립하는 것이 06의 설계이므로,
     * 페이징 계약 참조는 위반이 아니라 정상 경로다.
     *
     * <p><b>챕터 07 — 도메인 enum carve-out</b>: 근거와 범위는
     * {@link #apiModuleShouldBeDomainModelFree}와 동일하고, 짝 규칙
     * {@link #apiModuleShouldOnlyReadDomainEnums}가 호출 가능 메서드를 accessor로 제한한다.
     * 도메인 <b>모델</b>(애그리거트 루트·리포지토리·도메인 서비스)은 여전히 금지다.
     *
     * <p><b>챕터 10</b> — web-api는 admin(챕터 06)·ceo(챕터 09)보다 늦게 Response 승격을 받으므로
     * 이 carve-out도 이 챕터에서 뒤늦게 동기화됐다. 세 앱의 규칙 본문은 이제 동일하다.
     */
    @Test
    void controllersShouldBeDomainFree() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("ApiController")
            .should().dependOnClassesThat(
                resideInAPackage("com.tastyhouse.domain..")
                    .and(not(resideInAPackage("com.tastyhouse.domain.shared.page..")))
                    .and(not(domainEnum()))
            )
            .because("컨트롤러는 도메인 모델을 import하지 않는다(HTTP 경계는 Long·String). "
                + "페이징 계약과 도메인 enum만 carve-out");

        rule.check(classes);
    }

    /**
     * 소셜 로그인은 external-api의 SPI만 통해 쓴다. 제공자별 패키지의 wire DTO·클라이언트 구현에
     * 직접 의존하지 않는다.
     */
    @Test
    void shouldDependOnOauthSpiOnlyNotProviderPackages() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.tastyhouse.external.oauth.kakao..",
                "com.tastyhouse.external.oauth.naver..",
                "com.tastyhouse.external.oauth.facebook..",
                "com.tastyhouse.external.oauth.apple.."
            )
            .because("소셜 로그인은 com.tastyhouse.application.auth.port.out을 통해서만 사용한다");

        rule.check(classes);
    }

    /**
     * Request record는 domain-free·infra-free 순수 데이터 홀더다(검증 + Swagger 스키마).
     *
     * <p><b>챕터 02 개정</b> — {@code response/}는 QueryService가 조립하는 표현 계약이라
     * web-application으로 함께 이동했으므로, 이 모듈에 남은 대상은 {@code ..request..}뿐이다.
     * Response 쪽 동일 규칙은 web-application이 담당한다.
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
     * <p><b>챕터 02 개정</b> — 구체 서비스가 web-application으로 이동했으므로 이제 이 규칙 위반은
     * 애초에 컴파일되지 않는다(그 모듈의 {@code service} 패키지를 import해야 하므로). 그럼에도
     * 접미어 기준으로 남겨 두는 이유는, 누군가 구체 서비스를 web-api로 되돌리는 시도가 곧바로
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
     * <p><b>챕터 02 개정</b> — 대상 패키지를 {@code ..application.service..}에서
     * {@code com.tastyhouse.application..service..}로 갱신했다. 접미어가 아니라 <b>위치</b>로 잡으므로
     * {@code *Service} 같은 비표준 접미어 파사드({@code MemberService})까지 걸린다 — 실제로 이 규칙이
     * 2a 정규화 직후 {@code MemberApiController}/{@code MemberMeApiController}의 파사드 직접 주입을
     * 잡아냈고, {@code MemberScreenUseCase} 포트를 신설해 해소했다.
     */
    @Test
    void webAdaptersShouldNotDependOnApplicationServices() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..adapter.in.web..")
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.application..service..")
            .because("인바운드 어댑터는 UseCase 인터페이스만 주입한다(구체 서비스 금지)");

        rule.check(classes);
    }

    /**
     * <b>신설</b> — api 모듈에는 application 계층이 남아 있지 않다.
     *
     * <p>챕터 02의 물리 분리가 <b>되돌려지지 않았음</b>을 고정하는 규칙이다. 위 규칙들은 "컨트롤러가
     * 무엇을 주입하는가"를 보지만, 누군가 web-api 안에 {@code @Service} 빈을 새로 만들어 application
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
            .because("application 계층은 web-application 모듈이 소유한다(web-api에 @Service 금지)");

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


    /**
     * api 모듈은 도메인 모델을 알지 않는다(모듈 전역).
     *
     * <p>기존 {@code controllersShouldBeDomainFree}는 {@code *ApiController} 접미어,
     * {@code requestRecordsShouldBeDomainAndInfraFree}는 {@code ..request..} 패키지로 대상을 좁히므로
     * {@code config..}·{@code security..}·{@code exception..}이 <b>무검사 사각지대</b>였다 — 이 규칙이
     * 그 지점을 모듈 전역으로 봉인한다.
     *
     * <p><b>carve-out 3종</b>.
     * {@code domain.exception..}은 계층 칸이 없는 <b>횡단 관심사</b>이고({@code api-common-module}이
     * {@code api project(':domain-module')}로 공용 에러 계약을 전 모듈에 노출한다),
     * {@code domain.shared.page..}는 챕터 06이 페이징 조립을 컨트롤러로 옮기며 <b>정상 경로</b>가 됐다
     * (application이 {@code PageResult}를 반환하고 컨트롤러가 {@code PaginationResponse.from(...)}으로
     * 감싼다). 세 번째가 아래 <b>도메인 enum</b>이다.
     *
     * <p><b>챕터 07 — 도메인 enum의 읽기 전용 accessor는 위반이 아니다(타입 성격 술어).</b>
     * 챕터 06이 Response 조립을 컨트롤러로 올리면서 읽기 계약 {@code *Result}가 품은 도메인 enum을
     * {@code result.type().name()}으로 읽는 것이 <b>설계상 필연</b>이 됐다. 규칙의 원래 의도는
     * <b>승격 방향</b>(String·Long → 도메인 타입)을 막는 것인데 챕터 06이 옮긴 것은 <b>강등 방향</b>
     * (도메인 타입 → String)이고, ArchUnit 의존 그래프는 두 방향을 구분하지 못한다. 실측 위반 67건은
     * 전부 읽기 전용 accessor였고 도메인 객체 생성·상태 변경·리포지토리 접근은 0건이었다.
     *
     * <p><b>패키지 술어를 쓸 수 없다</b>: 도메인 enum 76개는 전부 {@code com.tastyhouse.domain.<ctx>.model}에
     * <b>애그리거트 루트와 같은 자리</b>에 있다. carve-out을 {@code resideInAPackage("..model..")}로 쓰면
     * {@code Shop}·{@code Order}까지 함께 열려 규칙이 무력해진다(선례: 패키지 술어 예외는 대상이 전부 그
     * 패키지에 살면 규칙을 삼킨다). 그래서 위치가 아니라 <b>타입 성격</b>({@code JavaClass#isEnum()})으로
     * 좁히고, {@code DOMAIN_ROOT} 패키지 조건을 함께 걸어 domain 밖 enum까지 열리지 않게 한다.
     *
     * <p><b>타입 수준 carve-out만으로는 이빨이 빠진다</b> — 도메인 enum은 무행위 값 집합이 아니다.
     * 76개 중 13개가 비즈니스 로직을 노출하며({@code MemberGrade#fromReviewCount} 등급 배정 규칙,
     * {@code OrderStatus#canTransitionTo} 상태 전이 가드), 이 규칙만 두면 컨트롤러가 그것을 호출해도
     * 빌드가 통과한다. 짝 규칙 {@link #apiModuleShouldOnlyReadDomainEnums}가 호출 가능 메서드를
     * accessor로 제한해 그 구멍을 막는다({@code commandRecordsShouldNotHoldMultipartFile}이 클래스 수준
     * {@code MultipartFile} carve-out을 필드 수준에서 막는 것과 같은 구조).
     *
     * <p><b>⚠️ 위반은 {@code import}로 보이지 않는다</b>: ArchUnit은 import 문이 아니라 바이트코드 상수
     * 풀을 읽으므로, 이 모듈에 {@code import com.tastyhouse.domain..}이 0건이어도 {@code *Result}
     * 컴포넌트를 통한 <b>전이 의존</b>으로 잡힌다. 그때 대상은 {@code java.lang.Enum}이 아니라 <b>구체
     * enum</b>이다(javac가 메서드 참조 소유자로 정적 수신 타입을 기록한다). <b>따라서 grep으로 검증하면
     * "위반 0건"으로 오판한다</b> — 검증은 반드시 이 테스트로 한다.
     *
     * <p>anchor가 모듈 전체({@code noClasses()})라 클래스가 존재하는 한 <b>대상 0건이 될 수 없다</b>.
     */
    @Test
    void apiModuleShouldBeDomainModelFree() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat(
                resideInAPackage("com.tastyhouse.domain..")
                    .and(not(resideInAPackage("com.tastyhouse.domain.exception..")))
                    .and(not(resideInAPackage("com.tastyhouse.domain.shared.page..")))
                    .and(not(domainEnum()))
                    .as("도메인 모델(enum·에러 계약·페이징 계약 제외)")
            )
            .because("api 모듈은 도메인 모델을 알지 않는다(승격은 application 서비스 담당). "
                + "enum은 짝 규칙이 읽기 accessor만 허용하는 조건으로 carve-out");

        rule.check(classes);
    }

    /**
     * <b>챕터 07 신설 짝 규칙</b> — api 모듈은 도메인 enum의 <b>읽기 전용 accessor만</b> 호출한다.
     *
     * <p>{@link #apiModuleShouldBeDomainModelFree}가 타입 수준에서 뚫어 둔 구멍을 이 규칙이 <b>메서드
     * 수준</b>에서 막는다 — {@code commandRecordsShouldBeBoundaryTyped}가 {@code MultipartFile}을 열고
     * {@code commandRecordsShouldNotHoldMultipartFile}이 필드 수준에서 막는 것과 같은 구조다.
     *
     * <p>도메인 enum 76개 중 <b>13개가 비즈니스 로직을 노출</b>한다 —
     * {@code MemberGrade#fromReviewCount}(등급 배정 규칙)·{@code MemberGrade#isHigherThanOrEqual}·
     * {@code OrderStatus#canTransitionTo}(상태 전이 가드)·{@code DayType#appliesTo}·
     * {@code ClosedDayType#matches}·{@code ReservationStatus#isBlocking} 등. 타입 성격 술어만 두면
     * 컨트롤러가 회원 등급을 계산하거나 주문 전이를 인가해도 빌드가 통과하고, <b>응답 JSON 스키마
     * 대조로는 잡히지 않는다</b>(값이 같으면 JSON이 동일하다). 그것을 잡는 것은 이 규칙뿐이다.
     *
     * <p>허용 목록은 {@link #ALLOWED_DOMAIN_ENUM_ACCESSORS}이며 바이트코드 그래프 실측에서 도출했다.
     */
    @Test
    void apiModuleShouldOnlyReadDomainEnums() {
        ArchRule rule = noClasses()
            .should().callMethodWhere(DescribedPredicate.describe(
                "도메인 enum의 비-accessor 호출",
                (JavaMethodCall call) -> domainEnum().test(call.getTargetOwner())
                    && !ALLOWED_DOMAIN_ENUM_ACCESSORS.contains(call.getName())))
            .because("api 모듈은 도메인 enum의 읽기 전용 accessor만 호출한다"
                + "(from(String) 승격·상태 전이 판정·등급 계산은 application·domain 담당)");

        rule.check(classes);
    }

    /**
     * <b>챕터 07 신설</b> — 위 두 규칙이 <b>여전히 물린다</b>는 영구 증명.
     *
     * <p>두 규칙은 현재 위반 0건이므로, carve-out을 잘못 넓혀(예: {@code isEnum()} 대신 {@code ..model..}
     * 패키지 술어로 되돌려) 규칙이 무력해져도 <b>그대로 통과한다</b>. 그 무력화를 잡는 것이 이 테스트다.
     * 스펙이 제안한 "일부러 위반 코드를 넣어 확인 후 되돌린다"는 한 번 확인하고 사라지므로, 동일 술어를
     * 조립해 판별력 자체를 상시 단정한다.
     *
     * <p>(4)가 특히 중요하다 — 술어를 {@code isEnum()}으로 좁힌 <b>이유 자체</b>(enum과 애그리거트 루트가
     * 같은 패키지에 산다)를 고정하므로, 전제가 바뀌면 낡은 주석이 아니라 실패로 드러난다.
     *
     * <p>domain 클래스는 이 모듈 테스트 클래스패스에 있다 — {@code api-common-module}이
     * {@code api project(':domain-module')}로 전이 노출한다.
     */
    @Test
    void domainBoundaryPredicatesShouldStillBite() {
        JavaClasses domainClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages(DOMAIN_ROOT);

        DescribedPredicate<JavaClass> forbidden = resideInAPackage("com.tastyhouse.domain..")
            .and(not(resideInAPackage("com.tastyhouse.domain.exception..")))
            .and(not(resideInAPackage("com.tastyhouse.domain.shared.page..")))
            .and(not(domainEnum()));

        JavaClass shop = domainClasses.get("com.tastyhouse.domain.shop.model.Shop");
        JavaClass memberGrade = domainClasses.get("com.tastyhouse.domain.member.model.MemberGrade");

        // (1) 애그리거트 루트는 여전히 금지 — carve-out을 패키지 술어로 되돌리면 여기서 실패한다.
        if (!forbidden.test(shop)) {
            throw new AssertionError(
                "carve-out이 너무 넓습니다 — 애그리거트 루트 Shop이 허용 대상이 됐습니다. "
                    + "enum 타입 성격 술어를 패키지 술어로 되돌리지 않았는지 확인하세요.");
        }

        // (2) 도메인 enum은 carve-out 대상이다.
        if (!memberGrade.isEnum() || forbidden.test(memberGrade)) {
            throw new AssertionError("도메인 enum MemberGrade가 carve-out되지 않았습니다.");
        }

        // (3) 짝 규칙이 막아야 할 대상이 실재한다 — 로직 메서드가 허용 목록 밖임을 단정.
        boolean hasLogicMethodOutsideAllowList = memberGrade.getMethods().stream()
            .anyMatch(method -> method.getName().equals("fromReviewCount")
                && !ALLOWED_DOMAIN_ENUM_ACCESSORS.contains(method.getName()));
        if (!hasLogicMethodOutsideAllowList) {
            throw new AssertionError(
                "전제가 바뀌었습니다 — MemberGrade#fromReviewCount가 없거나 허용 목록에 들어갔습니다. "
                    + "짝 규칙 apiModuleShouldOnlyReadDomainEnums가 무엇을 막는지 재검토하세요.");
        }

        // (4) 설계 전제 고정 — 도메인 enum이 애그리거트 루트와 같은 '..model' 패키지에 공존하기 때문에
        //     패키지 술어를 쓸 수 없다. 그 공존이 깨지면(enum 전용 패키지가 생기면) 술어를 단순화할 수
        //     있으므로, 전제를 주석이 아니라 단정으로 고정한다.
        long enumsInAggregatePackages = domainClasses.stream()
            .filter(JavaClass::isEnum)
            .filter(javaClass -> javaClass.getPackageName().startsWith(DOMAIN_ROOT))
            .filter(javaClass -> javaClass.getPackageName().endsWith(".model"))
            .count();
        if (enumsInAggregatePackages == 0) {
            throw new AssertionError(
                "전제가 바뀌었습니다 — '..model' 패키지에 도메인 enum이 더는 없습니다. "
                    + "enum이 자기 패키지로 분리됐다면 타입 성격 술어를 패키지 술어로 단순화할 수 "
                    + "있는지 재검토하세요.");
        }
        if (!shop.getPackageName().endsWith(".model")) {
            throw new AssertionError(
                "전제가 바뀌었습니다 — 애그리거트 루트 Shop이 '..model' 패키지에 없습니다: "
                    + shop.getPackageName());
        }
    }

    /**
     * 도메인 enum — 위치가 아니라 <b>타입 성격</b>으로 판별한다.
     *
     * <p>{@code isEnum()}에 {@code DOMAIN_ROOT} 패키지 조건을 함께 거는 이유는, 그냥 {@code isEnum()}이면
     * domain 밖 enum까지 대상이 되어 술어의 의미가 흐려지기 때문이다.
     *
     * <p><b>{@code domain.exception..}은 제외한다</b> — {@code ErrorCode}가 enum이라서 그냥 두면 짝 규칙
     * {@link #apiModuleShouldOnlyReadDomainEnums}이 전역 예외 핸들러의 {@code getCode()}·
     * {@code getDefaultMessage()} 호출을 잡는다(web-api에서 실측 2건). 에러 계약은 클래스 수준 규칙에서도
     * carve-out된 <b>횡단 관심사</b>이므로 두 규칙이 같은 예외를 공유해야 한다 — 이 술어를 두 규칙이
     * 함께 쓰는 이유이기도 하다.
     */
    private static DescribedPredicate<JavaClass> domainEnum() {
        return DescribedPredicate.describe("도메인 enum",
            javaClass -> javaClass.isEnum()
                && javaClass.getPackageName().startsWith(DOMAIN_ROOT)
                && !javaClass.getPackageName().startsWith(DOMAIN_ROOT + ".exception"));
    }


    /**
     * <b>챕터 01 신설 · 챕터 03 재작성 — 어댑터는 자기 앱의 application 슬라이스만 의존한다.</b>
     *
     * <p><b>이 규칙은 챕터 01이 없앤 컴파일 게이트를 대체한다.</b> 그전까지 이 모듈의 어댑터가 다른
     * 앱의 UseCase를 주입하는 것은 <b>빌드가</b> 막았다 — build.gradle에 자기 앱의 application 모듈
     * 하나만 있었으므로 다른 앱의 패키지는 클래스패스에 아예 없었다. 챕터 01이 4개 application 모듈을
     * {@code :application} 하나로 합치면서 4개 앱의 클래스가 <b>전부 이 모듈의 컴파일 클래스패스에
     * 들어왔다.</b>
     *
     * <p><b>챕터 03 재작성 — 판정 근거가 패키지에서 마커로 바뀌었다.</b> 챕터 01의 원본은 자기를 뺀
     * 3개 앱 패키지를 열거해 금지했는데, 평탄화로 그 패키지들이 사라졌다. 이제 소속의 근거는
     * {@link WebApp} 등 마커 애노테이션이므로 규칙도 마커로 판정한다.
     *
     * <p>세 갈래로 나눠 검사한다.
     * <ul>
     *   <li><b>(a) UseCase 인터페이스</b> — {@code ..port.in..}의 인터페이스에 의존한다면 그것이
     *       {@link WebApp}를 달고 있어야 한다. 마커를 인터페이스가 직접 가지므로 술어가 단순하다.</li>
     *   <li><b>(b) Command record</b> — record에는 마커가 없다. 소속을 {@link AppOwnership#derive}로
     *       <b>유도</b>해 그 집합이 {@link WebApp}인지 본다(유도 규칙은 그 클래스 Javadoc 참조).</li>
     *   <li><b>(c) 구체 서비스</b> — {@code @Service}/{@code @Component} 클래스 의존은 앱을 가릴 것도
     *       없이 전부 금지이며, 이미 {@code com.tastyhouse.application..service..} 패키지를 막는
     *       기존 규칙이 맡는다. 여기서 중복하지 않는다.</li>
     * </ul>
     *
     * <p>짝이 되는 규칙은 application 모듈의
     * {@code AppIsolationTest#appsShouldNotDependOnEachOther}다 — 그쪽이 application 계층끼리의 수평
     * 의존을, 이쪽이 어댑터 → 남의 application 의존을 막는다.
     */
    @Test
    void adaptersShouldOnlyUseOwnAppUseCases() {
        JavaClasses applicationClasses = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.tastyhouse.application");

        Map<String, Set<Class<? extends Annotation>>> commandApps = new HashMap<>();
        AppOwnership.derive(applicationClasses)
            .forEach((record, apps) -> commandApps.put(record.getName(), apps));

        List<String> violations = new ArrayList<>();
        for (JavaClass adapter : classes) {
            for (JavaClass dependency : adapter.getDirectDependenciesFromSelf().stream()
                .map(Dependency::getTargetClass).toList()) {

                if (!dependency.getPackageName().contains(".port.in")) {
                    continue;
                }
                if (dependency.isInterface()) {
                    if (!dependency.isAnnotatedWith(WebApp.class)) {
                        violations.add(adapter.getName() + " -> " + dependency.getName()
                            + " (다른 앱의 UseCase — @WebApp가 아니다)");
                    }
                } else if (dependency.isRecord()) {
                    Set<Class<? extends Annotation>> apps = commandApps.get(dependency.getName());
                    if (apps != null && !apps.equals(Set.of(WebApp.class))) {
                        violations.add(adapter.getName() + " -> " + dependency.getName()
                            + " (소속 앱 " + AppOwnership.describe(apps) + " — @WebApp가 아니다)");
                    }
                }
            }
        }

        assertThat(violations)
            .as("인바운드 어댑터는 자기 앱(@WebApp)의 application 슬라이스만 의존한다")
            .isEmpty();
    }
}
