package com.tastyhouse.application.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * application 모듈 레이어 경계 규칙(ArchUnit) — 4개 앱 공통 16종.
 *
 * <p><b>챕터 01로 {@code {web,admin,ceo,batch}-application} 4개 모듈이 이 모듈 하나로 합쳐졌다.</b>
 * 그 4벌의 {@code LayerRulesTest}는 규칙 이름·본문까지 동일했고(diff는 carve-out 이름과 {@code because}
 * 문구뿐이었다) 4벌을 유지할 이유가 없어 이 한 벌로 통합했다. 앱 고유 규칙은 분리해 두었다 —
 * batch 4종은 {@link BatchSchedulerRulesTest}, 앱 간 격리는 {@link AppIsolationTest}.
 *
 * <p>importer는 <b>4개 앱 패키지</b>를 함께 훑는다. 자바 패키지는 이 챕터에서 바뀌지 않았으므로
 * ({@code com.tastyhouse.{app}application}) 규칙 본문은 대부분 그대로 옮겨 왔고, 통합으로 의미가
 * 달라지는 두 곳만 손봤다 — {@code queryServicesShouldNotDependOnWritePorts}의 carve-out을 FQN으로
 * 바꾼 것(아래 참조)과 {@code applicationMustNotDependOnAdapters}의 금지 대상을 4개 api 패키지로
 * 넓힌 것이다.
 *
 * <p>infra·QueryDSL 차단은 build.gradle이 1차로 막는다(이 모듈은 infrastructure를 컴파일
 * 클래스패스에 두지 않는다). 그럼에도 규칙을 남기는 이유는 회귀 방어다 — 누군가 build.gradle에
 * 의존 한 줄을 되돌리면 컴파일은 통과하고 계층만 조용히 무너진다.
 *
 * <p>{@code allowEmptyShould(true)}는 이 파일 어디에도 쓰지 않는다. 규칙이 대상을 잃으면
 * 공허하게 통과시키지 말고 규칙을 지우거나 anchor를 고친다({@link RuleAnchorTest}가 자동 검증).
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.webapplication", "com.tastyhouse.adminapplication",
                        "com.tastyhouse.ceoapplication", "com.tastyhouse.batchapplication");

    /**
     * 이 모듈이 소유한 앱 단독 읽기 계약(split package) 271개.
     *
     * <p>위 {@code classes}와 분리하는 이유는, 기존 규칙들이 {@code com.tastyhouse.application..port.out..}을
     * <b>외부</b>로 취급하기 때문이다. 한 importer에 합치면 계약 자신이 그 규칙의 대상이 되어 의미가 뒤집힌다.
     *
     * <p>이 importer는 domain-module jar의 공유 계약 55개도 함께 잡는다 — split package라 패키지만으로는
     * 소유 모듈을 가릴 수 없다. 챕터 04에서 그 55개가 이 모듈로 돌아오면 구분 자체가 사라진다.
     */
    private final JavaClasses readContracts = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    /**
     * CQRS 교차 주입 금지(명령 → 조회).
     *
     * <p>{@code *CommandService}는 읽기 포트({@code com.tastyhouse.application..port.out..})와
     * 같은 모듈의 {@code *QueryService}를 주입하지 않는다 — 명령 경로가 표현용 투영에 결합되면
     * 클래스는 둘로 나뉘었지만 의존 그래프는 여전히 하나로 뭉쳐 있어 CQRS 분리가 이름만 남는다.
     */
    @Test
    void commandServicesShouldNotDependOnQueryDaos() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("CommandService")
            .should().dependOnClassesThat().resideInAPackage(
                "com.tastyhouse.application..port.out..")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("QueryService")
            .because("CommandService는 조회 어댑터도 읽기 포트도 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    /**
     * CQRS 교차 주입 금지(조회 → 쓰기).
     *
     * <p>{@code *QueryService}는 domain-module의 write 포트를 주입하지 않는다 — 조회 트랜잭션
     * ({@code readOnly = true})에서 쓰기 경로가 열리는 것을 구조적으로 막는다.
     *
     * <p><b>carve-out 3건은 각 앱에서 그대로 승계한 확정 판정</b>이며, 이관 대상이 아니다.
     * <ul>
     *   <li>web {@code ShopQueryService} — write 포트를 배달팁 계산 경로가 도메인 서비스에 넘길
     *       애그리거트 로드에 쓴다. 표현용 투영이 아니라 <b>도메인 계산 입력</b>이다.</li>
     *   <li>admin {@code AdminQueryService} — 인증(UserDetails 로드)·시드 멱등성 확인에 쓰이며
     *       표현 목적 read model이 없다. 엔티티/원시값 반환 + 불변식 검증 경로다.</li>
     *   <li>ceo {@code CeoQueryService} — 위 admin과 같은 인증 조회 경로다.</li>
     * </ul>
     *
     * <p><b>챕터 01 개정 — 판정 기준을 simple name에서 FQN으로 바꿨다.</b> 4개 모듈이 하나로 합쳐지면서
     * 동명 클래스가 한 importer에 들어왔기 때문이다. 예컨대 {@code ShopQueryService}는 web·admin·ceo에
     * 각각 존재하므로, 기존의 {@code haveSimpleNameNotEndingWith("ShopQueryService")}를 그대로 두면
     * <b>의도한 web 1개가 아니라 3개 전부가 면제</b>되어 admin·ceo의 위반이 조용히 통과한다.
     * FQN으로 못 박으면 면제 대상이 정확히 3개로 유지된다(챕터 02의 개명으로 동명 충돌이 해소되면
     * 이 표현은 다시 단순해질 수 있으나, 그때도 FQN이 더 정확하므로 유지한다).
     *
     * <p>이 목록에 새 항목을 추가하지 않는다.
     */
    @Test
    void queryServicesShouldNotDependOnWritePorts() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("QueryService")
            .and().doNotHaveFullyQualifiedName("com.tastyhouse.webapplication.shop.service.ShopQueryService")
            .and().doNotHaveFullyQualifiedName("com.tastyhouse.adminapplication.admin.service.AdminQueryService")
            .and().doNotHaveFullyQualifiedName("com.tastyhouse.ceoapplication.ceo.service.CeoQueryService")
            .should().dependOnClassesThat().resideInAnyPackage("com.tastyhouse.domain..repository..")
            .because("QueryService는 write 포트를 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    /**
     * {@code *CommandService}는 인바운드 포트를 최소 1개 구현한다.
     *
     * <p>batch에는 {@code *CommandService}가 없고 잡 본문이 {@code *SchedulerService}에 담기므로,
     * 그쪽 대응 규칙은 {@link BatchSchedulerRulesTest#schedulerServicesShouldImplementUseCase()}가 맡는다.
     */
    @Test
    void commandServicesShouldImplementUseCase() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("CommandService")
            .should().implement(resideInAPackage("..port.in.."))
            .because("CommandService는 대응 CommandUseCase를 구현한다");

        rule.check(classes);
    }

    /**
     * {@code *QueryService}는 인바운드 포트를 최소 1개 구현한다.
     */
    @Test
    void queryServicesShouldImplementUseCase() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("QueryService")
            .should().implement(resideInAPackage("..port.in.."))
            .because("QueryService는 대응 QueryUseCase를 구현한다");

        rule.check(classes);
    }

    /**
     * Command record는 경계 타입만 싣는다(carve-out 3건 그대로 유지).
     *
     * <p>importer를 <b>web·admin·ceo 3개 앱으로 한정</b>한다. batch에는 Command record가 없고
     * 인바운드 포트가 {@code void foo()} 뿐이라 carve-out이 {@code domain.exception..} 하나인
     * <b>엄격판</b>을 쓸 수 있으며, 그쪽은 {@link BatchSchedulerRulesTest#inboundPortsShouldBeBoundaryTyped()}가
     * 맡는다. 한 importer로 합치면 batch가 느슨한 3-carve-out 규칙에 얹혀 엄격함을 잃는다.
     *
     * <p>{@code com.tastyhouse.domain.exception..}은 예외로 허용한다 — {@code BusinessException}·
     * {@code ErrorCode}는 애그리거트가 아니라 전 계층이 공유하는 <b>횡단 관심사(에러 계약)</b>이고,
     * compact constructor의 구조적 가드가 이를 던져야 응답 코드가 나머지 경로와 같은 형태로 나간다.
     *
     * <p>{@code org.springframework.web.multipart..}({@code MultipartFile})도 제외한다 — 업로드를 받는
     * 연산은 {@code method(XxxCommand, MultipartFile)}처럼 별도 파라미터로 두는 것이 규정된 형태이고,
     * ArchUnit 의존 그래프는 같은 패키지 UseCase 인터페이스의 메서드 파라미터까지 함께 잡는다.
     * Command 필드로 실리는 것은 아래 {@code commandRecordsShouldNotHoldMultipartFile}이 따로 막는다.
     *
     * <p>{@code com.tastyhouse.domain.shared.page..}는 세 번째 carve-out이다(챕터 06이 admin에 세우고
     * 09가 ceo, 10이 web에 확대). 근거는 위 {@code MultipartFile} carve-out과 <b>동일한 구조</b>다:
     * 이 규칙이 겨냥하는 것은 Command record가 <b>필드로</b> 도메인 모델을 싣는 것인데, ArchUnit은 같은
     * {@code ..port.in..} 패키지에 사는 <b>QueryUseCase의 메서드 시그니처</b>까지 함께 잡는다. 목록 반환
     * 타입이 {@code PaginationResponse}(표현 계약)에서 {@code PageResult}(도메인 페이징 계약)로 바뀌면서
     * 걸린 건들은 <b>전부 반환 타입이며 Command 필드는 한 건도 없다</b>(실측 확인).
     *
     * <p>즉 이것은 규칙을 무르게 하는 것이 아니라, 규칙이 애초에 겨냥하지 않던 대상을 제외하는 것이다.
     * 도메인 <b>모델</b>({@code domain.{shop,order,member}.model..} 등)은 그대로 금지이며, Command가
     * 실제로 도메인 타입을 필드로 실으면 여전히 걸린다.
     */
    @Test
    void commandRecordsShouldBeBoundaryTyped() {
        JavaClasses appsWithCommands = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.tastyhouse.webapplication", "com.tastyhouse.adminapplication",
                            "com.tastyhouse.ceoapplication");

        ArchRule rule = noClasses()
            .that().resideInAPackage("..port.in..")
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "com.tastyhouse.domain..",
                    "com.tastyhouse.infrastructure..",
                    "org.springframework.web.."
                ).and(not(resideInAPackage("com.tastyhouse.domain.exception..")))
                 .and(not(resideInAPackage("com.tastyhouse.domain.shared.page..")))
                 .and(not(resideInAPackage("org.springframework.web.multipart..")))
            )
            .because("Command는 도메인 모델·infra·web 타입을 싣지 않는다"
                + "(에러 계약·페이징 계약은 횡단 관심사라 예외)");

        rule.check(appsWithCommands);
    }

    /**
     * Command record의 <b>필드</b>로 {@code MultipartFile}을 싣지 않는다.
     *
     * <p>위 규칙이 UseCase 메서드 파라미터를 허용하느라 뚫어 둔 구멍을 이 규칙이 필드 수준에서 막는다.
     * Command에는 업로드 <em>결과 참조</em>(파일 식별자·URL)만 담는다.
     */
    @Test
    void commandRecordsShouldNotHoldMultipartFile() {
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..port.in..")
            .should().notHaveRawType("org.springframework.web.multipart.MultipartFile")
            .because("Command 필드로 업로드 타입을 싣지 않는다(업로드는 UseCase 메서드의 별도 파라미터)");

        rule.check(classes);
    }

    /**
     * 인바운드 포트는 web 플럼빙을 알지 않는다.
     */
    @Test
    void portInShouldNotDependOnWebPlumbing() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..port.in..")
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
     * {@code *CommandService}는 {@code ..request..}를 알지 않는다.
     *
     * <p>물리 분리 후에는 {@code request/}가 각 api 모듈에 남아 컴파일 자체가 불가능하므로 이 규칙은
     * 아래 {@code applicationMustNotDependOnAdapters}와 이중 방어가 된다. 그럼에도 남기는 이유는,
     * 누군가 request record를 이 모듈로 끌어오는 방식으로 우회할 때 그 시도가 곧바로 드러나게
     * 하기 위해서다.
     */
    @Test
    void commandServicesShouldNotDependOnRequestRecords() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("CommandService")
            .should().dependOnClassesThat().resideInAnyPackage("..request..")
            .because("CommandService는 Request record를 받지 않는다(매핑은 컨트롤러가 소유)");

        rule.check(classes);
    }

    /**
     * QueryDSL 차단. 회귀 방어 규칙이다.
     */
    @Test
    void shouldNotDependOnQuerydsl() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.querydsl..");

        rule.check(classes);
    }

    /**
     * infrastructure 차단. 회귀 방어 규칙이다.
     *
     * <p>build.gradle이 이미 1차로 막지만(이 모듈은 infrastructure를 의존하지 않는다),
     * 의존 한 줄이 되돌아오면 컴파일은 통과하고 계층만 조용히 무너지므로 규칙으로 고정한다.
     */
    @Test
    void shouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure..");

        rule.check(classes);
    }

    /**
     * application 계층은 서블릿을 알지 않는다.
     *
     * <p>{@code MultipartFile}({@code org.springframework.web.multipart..})만 예외다 — 업로드를 받는
     * UseCase 메서드의 경계 파라미터로 존치한다. 이 예외가 auth 컨텍스트가 이 모듈에 있을 수 있는
     * 게이트이기도 하다: {@code TokenService}·{@code JwtTokenProvider}·{@code CustomUserDetails}·
     * {@code AdminUserDetailsService}·{@code CeoUserDetailsService}는 Spring Security core와 JWT
     * 타입만 쓰는 <b>서블릿-프리</b> 타입이라 여기 있고, 서블릿 결합 타입(필터·EntryPoint·
     * {@code JwtConfig}·{@code RedisRepositoryConfig}·{@code PublicPaths}·{@code SecurityConfig})은
     * 각 api 모듈에 남았다.
     *
     * <p>ceo는 이 예외가 특히 실질적이다 — 가게 이미지 변경·콘텐츠보드·메뉴 이미지 서비스와 규격
     * 검증기({@code ShopImageSpecValidator}·{@code ProductImageSpecValidator})가 업로드를 정당하게
     * 파라미터로 받는다. 검증기가 쓰는 {@code javax.imageio.ImageIO}는 java 표준이라 걸리지 않는다.
     *
     * <p>batch에는 업로드가 없어 예외 없는 완전한 servlet-free를 표현할 수 있으므로, 더 엄격한
     * batch 전용판을 {@link BatchSchedulerRulesTest}가 아니라 이 규칙과 별개로 두지 않고
     * {@code applicationServicesShouldNotDependOnWebLayer}가 서비스 이름에 anchor 해 보완한다.
     *
     * <p><b>{@code TokenService}·{@code AuthCommandService}가 앱마다 중복되는 것은 의도된 앱별
     * 중복이다</b>(인증 주체 {@code Member}·{@code Admin}·{@code Ceo}, 앱별 ErrorCode,
     * {@code JWT_SECRET_*} 분리 — backend/CLAUDE.md 허용 목록). 통합하지 않는다.
     */
    @Test
    void applicationMustBeServletFree() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "jakarta.servlet..",
                    "org.springframework.web.."
                ).and(not(resideInAPackage("org.springframework.web.multipart..")))
            )
            .because("application 계층은 서블릿·spring-web 플럼빙을 알지 않는다(업로드 경계 타입만 예외)");

        rule.check(classes);
    }

    /**
     * application은 인바운드 어댑터를 역참조하지 않는다.
     *
     * <p>의존 방향은 {api 모듈} → application 한 방향뿐이다. 이 규칙이 없으면 서비스가
     * 컨트롤러·Request record를 다시 끌어와 물리 분리가 이름만 남는다.
     *
     * <p><b>챕터 01 개정 — 금지 대상을 4개 api 패키지 전부로 넓혔다.</b> 4벌이 각각 자기 앱의
     * api 패키지 하나만 막던 것을 합치면서, 한 벌이 4개를 모두 막는 형태가 됐다. 어느 앱의 서비스든
     * 어느 api 모듈도 역참조할 수 없다는 뜻이라 오히려 강해진 것이다.
     */
    @Test
    void applicationMustNotDependOnAdapters() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                "com.tastyhouse.webapi..",
                "com.tastyhouse.adminapi..",
                "com.tastyhouse.ceoapi..",
                "com.tastyhouse.batch..")
            .because("application은 인바운드 어댑터(web-api·admin-api·ceo-api·batch-module)를 역참조하지 않는다");

        rule.check(classes);
    }

    /**
     * 읽기 계약은 프레임워크-프리를 유지한다.
     *
     * <p>이 계약들은 원래 {@code application-common-module}에 있었고, 그 모듈이 루트 build.gradle의
     * spring-boot-starter 일괄 적용에서 <b>제외</b>돼 있어 {@code org.springframework} import가 아예
     * 컴파일 에러였다 — 프레임워크-프리가 빌드 게이트로 강제되던 것이다. 계약이 앱 모듈로 옮겨오면서
     * 그 게이트가 사라졌다(이 모듈은 starter·web·security를 모두 받는다). 잃어버린 강제를 규칙으로 되살린다.
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

    /**
     * application 계층은 Swagger를 알지 않는다.
     *
     * <p>챕터 06·09·10 이전에는 {@code *QueryService}가 {@code @Schema}가 붙은 {@code *Response}를
     * 직접 조립했다(admin 85 · ceo 105 · web 131, 총 321개). 유스케이스 계층이 <b>HTTP 표현 포맷과
     * API 문서화 도구를 아는 상태</b>였다는 뜻이다. 그 챕터들이 Response를 각 api 모듈로 승격하고
     * 유스케이스는 프레임워크-프리 {@code *Result}·{@code PageResult}를 반환하도록 바꿨다.
     *
     * <p>이 규칙이 그 상태를 고정한다. 규칙이 없으면 다음에 컨텍스트를 추가하는 사람이 예전 모양대로
     * QueryService에서 Response를 조립해도 빌드가 통과한다 — 승격이 조용히 되돌아간다.
     */
    @Test
    void applicationShouldNotDependOnSwagger() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("io.swagger..")
            .because("유스케이스 계층은 API 문서화 도구를 알지 않는다(Response 조립은 각 api 모듈 담당)");

        rule.check(classes);
    }

    /**
     * application 계층은 표현 모듈(api-common-module)을 알지 않는다.
     *
     * <p>{@code PaginationResponse}·{@code ApiResponse} 같은 HTTP 래퍼는 표현 계약이다. 이 계층이
     * 그것을 조립하던 것이 Response 승격 챕터들이 걷어낸 두 번째 위반이며(페이징은 이제
     * {@code PageResult}로 반환하고 컨트롤러가 {@code PaginationResponse.from(...)}으로 감싼다),
     * 이 규칙이 재발을 막는다.
     *
     * <p>챕터 11로 build.gradle의 api-common 의존이 끊겼으므로, 지금은 <b>의존 제거가 1차 방어선이고
     * 이 규칙이 회귀 방어인 이중화 상태</b>다.
     */
    @Test
    void applicationShouldNotDependOnApiCommon() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.apicommon..")
            .because("유스케이스 계층은 표현 모듈(api-common-module)을 알지 않는다");

        rule.check(classes);
    }

}
