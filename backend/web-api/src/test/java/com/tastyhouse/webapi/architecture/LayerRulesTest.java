package com.tastyhouse.webapi.architecture;

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
     * <p><b>이 규칙을 패키지 전면 금지로 상향하지 않는 근거(§1.3)</b>: 완전 매핑 전환으로
     * {@code request/}·{@code response/}가 {@code ..adapter.in.web..} 하위로 이동했으므로, 이 규칙을
     * "{@code ..application.service..}는 {@code ..adapter.in.web..}에 의존 금지"로 상향하면
     * <b>QueryService가 Response를 조립하는 확정 구조가 곧바로 위반</b>이 된다. Response 조립을
     * QueryService의 private 매퍼가 담당하는 것은 이 저장소의 확정 규칙이므로(DTO 조립 규칙), 그 구조를
     * 깨지 않도록 이 규칙은 <b>spring web 플럼빙 금지 수준을 유지</b>한다. 즉 서비스는 Response record를
     * 참조해도 되지만 요청 바인딩·서블릿·{@code HttpStatus}는 알 수 없다.
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
     * 읽기 포트({@code com.tastyhouse.application..port.out..} — QueryPort·Result·SearchCondition)를
     * 알 이유가 없다.
     *
     * <p>이 규칙이 없으면 <b>구조적 구멍</b>이 남는다 — 위
     * {@code controllersShouldNotDependOnRepositories}는 이름 접미어 {@code *Repository}만 매칭해
     * {@code *QueryPort}를 놓치고, {@code shouldNotDependOnInfrastructurePersistence}는
     * {@code ..persistence..}만 차단한다. 즉 컨트롤러가 읽기 포트를 직접 주입해도 기존 규칙 어디에도
     * 잡히지 않는다. 현재 위반은 0건이며, 이 규칙은 그 상태를 고정한다(기존 규칙은 이중 방어로 유지).
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
     * (읽기 포트 {@code com.tastyhouse.application..port.out..})와 같은 모듈의 {@code *QueryService}를
     * 주입하지 않는다 — 명령 경로가 표현용 투영에 결합되면 클래스는 둘로 나뉘었지만 의존 그래프는
     * 여전히 하나로 뭉쳐 있어 CQRS 분리가 이름만 남는다. 명령은 식별자만 반환하고 응답이 필요하면
     * 컨트롤러가 커밋 후 QueryService로 재조회한다.
     *
     * <p>과거 {@code ReviewCommandService}가 리뷰 작성 시 {@code MemberQueryDao}·{@code ProductQueryService}를
     * 직접 주입해 예외로 제외돼 있었으나, P3에서 해소되어(상품→가게 역조회는 write 포트
     * {@code ProductRepository#findById}로, 등록 응답 조립은 컨트롤러의 {@code ReviewQueryService} 재조회로)
     * <b>예외 목록이 비었다</b>. 이 모듈의 모든 {@code *CommandService}가 규칙 대상이다.
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
     * CQRS 교차 주입 금지(조회 → 쓰기). {@code *QueryService}는 domain-module의 write 포트
     * ({@code com.tastyhouse.domain..repository..})를 주입하지 않는다 — 조회 트랜잭션
     * ({@code readOnly = true})에서 쓰기 경로가 열리는 것을 구조적으로 막는다. 조회는
     * 읽기 포트({@code com.tastyhouse.application..port.out..})만 쓴다.
     *
     * <p><b>남은 제외 클래스는 이관 대상이 아니라 확정된 carve-out이다.</b> 챕터 04로 표현 목적
     * 조회는 전부 읽기 포트로 이관됐고, 여기 남은 것은 <a href="#">write 포트 잔류 판정 기준</a>에
     * 해당하는 것들뿐이다 — 도메인 계산 입력이거나 인증·불변식 검증 경로라 표현용 투영이 아니다.
     * 따라서 이 목록은 <b>비워야 할 부채가 아니며</b>, 새 항목을 추가하지 않는 것만 지킨다.
     *
     * <p>{@code ceo-api}의 {@code ShopOwnershipValidator}처럼 내부에 write 포트를 보유한 협력 빈을
     * QueryService가 주입하는 <em>간접</em> 위반은 이 규칙으로 잡히지 않는다. 직접 주입만 규칙화한다.
     */
    @Test
    void queryServicesShouldNotDependOnWritePorts() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("QueryService")
            // 챕터 04로 표현 목적 조회는 전부 읽기 포트로 이관됐다(북마크 존재·개인정보·팔로우 카운트·
            // 리뷰 작성 화면 접근 판정 등). 남은 것은 ShopQueryService 하나뿐이며, 그 write 포트는
            // 배달팁 계산 경로가 도메인 서비스(ShopDeliveryTipCalculator)에 넘길 애그리거트·도메인 모델을
            // 로드하는 데 쓴다 — 표현용 투영이 아니라 도메인 계산 입력이므로 이관 대상이 아니다
            // (스펙 §4의 "도메인 불변식 검증용 조회는 이관 대상이 아니다"에 해당).
            .and().haveSimpleNameNotEndingWith("ShopQueryService")
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
     * <p>예외·제외 항목은 없다. 페이징 컨트롤러도 QueryService가 돌려주는
     * {@code PaginationResponse<T>}(api-common-module)를 해체해 {@code ApiResponse.success(...)}
     * 4-인자 호출에 넘기므로 {@code domain.shared.page.PageResult}를 알 필요가 없고,
     * 결제 취소도 서비스가 응답 record를 돌려주므로 컨트롤러가 도메인 enum을 받지 않는다.
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
     * 소셜 로그인은 external-api의 SPI({@code com.tastyhouse.external.oauth.spi})만 통해 쓴다.
     * 제공자별 패키지({@code ..oauth.kakao..} 등)의 wire DTO·클라이언트 구현에 직접 의존하지 않는다 —
     * 직접 의존하면 제공자 API 응답 스키마 변경이 표현 계층까지 번지고, 과거처럼 외부 응답 DTO가
     * 도메인 타입({@code MemberGender})을 보유하는 역방향 결합이 되살아나기 쉽다.
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
            .because("소셜 로그인은 com.tastyhouse.external.oauth.spi를 통해서만 사용한다");

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
     * 컨트롤러는 인바운드 포트(UseCase 인터페이스)만 주입하고 {@code *CommandService}·
     * {@code *QueryService} 구체 클래스를 알지 않는다. 이 규칙이 완전 매핑 전환의
     * <b>컴파일 게이트</b>다 — 인터페이스를 만들어 두어도 컨트롤러가 구체 클래스를 계속 주입하면
     * 경계는 이름만 남는다.
     *
     * <p>챕터 03에서 {@code *QueryService}까지 확장했다. 이제 컨트롤러는 Command/Query 어느 쪽
     * 구체 서비스도 주입하지 않는다. 서비스끼리의 협력 주입(예: QueryService가 다른 QueryService를
     * 주입)은 컨트롤러가 아니므로 이 규칙의 대상이 아니다.
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
     * {@code *QueryService}는 인바운드 포트를 최소 1개 구현한다. 위
     * {@code commandServicesShouldImplementUseCase}의 읽기 경로 짝이다 — 컨트롤러가 구체 클래스를
     * 안 볼 뿐 서비스가 아무 인터페이스도 구현하지 않는 상태를 막는다.
     *
     * <p>조회 입력은 Command record로 묶지 않는다(챕터 03 스펙). 인터페이스는 기존 시그니처를
     * 그대로 싣고, 읽기 포트화(DAO → QueryPort 교체)는 챕터 04의 몫이다.
     */
    @Test
    void queryServicesShouldImplementUseCase() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("QueryService")
            .should().implement(resideInAPackage("..application.port.in.."))
            .because("QueryService는 대응 QueryUseCase를 구현한다");

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
     * <p>챕터 02의 중첩 Command record 치환이 끝나 <b>봉인 목록이 비었다</b>. 이 모듈의 모든
     * {@code *CommandService}가 예외 없이 규칙 대상이다.
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
     * 인바운드 어댑터({@code ..adapter.in.web..})는 {@code ..application.service..}의 구체 클래스에
     * 의존하지 않는다 — UseCase 인터페이스만 주입한다.
     *
     * <p>이것이 §1.3의 <b>패키지 규칙 승격</b>이다. 위 {@code controllersShouldDependOnUseCasesOnly}는
     * 클래스명 접미어({@code *CommandService}/{@code *QueryService})로만 대상을 잡아, 접미어가 다른
     * 구체 서비스는 그물을 빠져나간다. 실제로 {@code FollowApiController}가 파사드 {@code FollowService}를
     * 주입하고 있었는데 접미어가 달라 어느 규칙에도 걸리지 않았다(이 규칙 도입 시 컨트롤러를
     * {@code FollowCommandUseCase}/{@code FollowQueryUseCase} 직접 주입으로 바꾸고 파사드를 삭제했다).
     * 패키지가 계층을 표현하게 됐으므로 접미어가 아니라 <b>위치</b>로 잡는다. 접미어 규칙은 이중 방어로
     * 그대로 유지한다.
     */
    @Test
    void webAdaptersShouldNotDependOnApplicationServices() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..adapter.in.web..")
            .should().dependOnClassesThat().resideInAPackage("..application.service..")
            .because("인바운드 어댑터는 UseCase 인터페이스만 주입한다(구체 서비스 금지)");

        rule.check(classes);
    }

    /**
     * {@code ..application.port.in..}은 web 플럼빙·domain 모델·infrastructure에 의존하지 않는다 —
     * 위 {@code commandRecordsShouldBeBoundaryTyped}(도메인·infra·web 타입)와
     * {@code portInShouldNotDependOnWebPlumbing}(요청 바인딩·서블릿)을 §1.3에 따라 <b>패키지 기준
     * 하나로 통합</b>한 형태이며, 두 원본 규칙은 이중 방어로 유지한다.
     *
     * <p>예외 두 가지는 원본 규칙과 동일하다 — {@code com.tastyhouse.domain.exception..}(에러 계약은
     * 계층 칸이 없는 횡단 관심사)과 {@code org.springframework.web.multipart..}(UseCase 메서드의 업로드
     * 경계 파라미터). 근거는 각 원본 규칙의 Javadoc에 있다.
     */
    @Test
    void portInShouldBeFreeOfWebDomainAndInfrastructure() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..application.port.in..")
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "com.tastyhouse.domain..",
                    "com.tastyhouse.infrastructure..",
                    "org.springframework.web..",
                    "jakarta.servlet.."
                ).and(not(resideInAPackage("com.tastyhouse.domain.exception..")))
                 .and(not(resideInAPackage("org.springframework.web.multipart..")))
            )
            .because("인바운드 포트는 web 플럼빙·domain 모델·infrastructure를 알지 않는다");

        rule.check(classes);
    }

}
