package com.tastyhouse.infrastructure.architecture;

import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
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
 * infrastructure-module 레이어 경계 규칙(ArchUnit).
 *
 * <p>이 모듈에는 그동안 ArchUnit 의존 자체가 없어, 기존 가드 2종
 * ({@code QueryResultRecordVisibilityTest}·{@code EmbeddedRecordComponentOrderTest})이
 * 수제 리플렉션 스캔으로 <em>런타임 규약</em>(record 가시성·컴포넌트 순서)만 지키고 있었다.
 * 그 둘은 클래스패스 스캔 방식이 이미 잘 동작하므로 그대로 두고, 여기에는 그 방식으로 표현할 수 없는
 * <em>계층 방향</em> 규칙(모듈 방향, read→write 단방향)만 둔다.
 *
 * <p>api 모듈 4개의 동명 테스트와 마찬가지로 {@code allowEmptyShould(true)}를 쓰지 않는다 —
 * 규칙이 대상 0건으로 공허하게 통과하면 그 자체가 실패로 드러나야 한다.
 */
class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.infrastructure");

    /**
     * 모듈 방향: infrastructure는 api 모듈(web/admin/ceo/batch)을 알지 않는다.
     *
     * <p>의존 방향은 api → infrastructure → domain 한 방향이며, 역방향 의존이 생기면 조회 어댑터가
     * 특정 소비자의 표현 계약에 결합되어 "도메인당 DAO 1개, 소비자별 메서드로 분리"라는 소유 규칙이
     * 무너진다. 빌드 그래프상 infrastructure는 api 모듈을 의존하지 않아 컴파일 단계에서 이미 막히지만,
     * 이 규칙은 그 사실을 테스트로 명시해 향후 의존 추가 시 즉시 드러나게 한다.
     *
     * <p>{@code ..listener..}(도메인 이벤트 구독)도 이 규칙으로 함께 커버된다 — 리스너가
     * {@code @TransactionalEventListener} 규약을 지키는지(파라미터가 도메인 이벤트 타입인지)까지는
     * 강제하지 않되, api 모듈 의존 금지만은 여기서 보장한다.
     *
     * <p><b>챕터 03 개정 — 앱 패키지 4개 열거가 {@code com.tastyhouse.application..} 하나가 됐다.</b>
     * 과거에는 {@code com.tastyhouse.{web,admin,ceo,batch}application..}을 하나씩 나열했다.
     * "{@code com.tastyhouse.*application..}처럼 뭉뚱그리면 읽기 계약 패키지
     * {@code com.tastyhouse.application..}까지 걸린다"는 것이 그 이유였는데, 평탄화로 <b>둘이 같은
     * 패키지가 됐으므로</b> 열거로는 더 이상 구분할 수 없다.
     *
     * <p>그래서 판정을 뒤집는다 — {@code com.tastyhouse.application..} 전체를 금지하되
     * {@code ..port.out..}만 예외로 뺀다. <b>infra는 application의 아웃바운드 포트({@code port.out})를
     * 구현한다</b>는 것이 정방향이고({@code queryDaosShouldImplementQueryPorts}가 그것을 강제한다),
     * 유스케이스({@code port.in}·{@code service})는 침범 금지라는 뜻이다. 열거보다 오히려 정확해졌다 —
     * 과거 표현은 앱 패키지에 살지 않는 application 클래스를 그대로 통과시켰다.
     */
    @Test
    void shouldNotDependOnApiModules() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "com.tastyhouse.webapi..",
                    "com.tastyhouse.adminapi..",
                    "com.tastyhouse.ceoapi..",
                    "com.tastyhouse.batch..",
                    "com.tastyhouse.application.."
                ).and(not(resideInAPackage("com.tastyhouse.application..port.out.."))))
            .because("의존 방향은 api → infrastructure → domain 한 방향이다 — "
                + "infra는 application의 아웃바운드 포트(port.out)를 구현하고 유스케이스는 침범하지 않는다");

        rule.check(classes);
    }

    /**
     * read→write 단방향: {@code ..persistence..}(write 어댑터)는 {@code ..query..}(read 모델)를
     * 의존하지 않는다.
     *
     * <p>반대 방향({@code ..query..} → {@code ..persistence..})은 <b>정상</b>이다 — DAO가 같은 모듈의
     * {@code QXxxJpaEntity}를 static import해 조인하는 것이 조회 구현의 기본 형태다. 금지하는 것은 그
     * 역방향으로, write 경로가 표현용 투영에 결합되면 api 모듈에서 막아 둔 CQRS 교차 주입 금지
     * ({@code commandServicesShouldNotDependOnQueryDaos})가 infra 안쪽에서 우회된다.
     *
     * <p><b>봉인 목록</b>: 규칙 도입 시점의 위반 3건은 전부 <em>도메인 출력 포트 어댑터</em>다 —
     * 도메인이 선언한 포트를 구현하면서 그 데이터의 소유 도메인이 이미 갖고 있는 read model을 재사용한다
     * (예: 랭킹 집계용 리뷰 수는 리뷰 도메인 소유라 {@code review/query/}에 있고, 랭킹 포트 어댑터가 그것을
     * 도메인 값 타입으로 옮겨 담는다). 이들은 write 경로가 아니라 <em>포트 구현</em>이므로 위 위험에
     * 해당하지 않지만, 패키지 위치({@code ..persistence..})가 규칙의 표현과 어긋나 잡힌다. 규칙 전체를
     * 끄지 않고 {@code ErrorCodeConventionTest}·{@code ContextBoundaryTest}의 봉인 목록 선례대로 클래스명으로
     * 명시 제외하며, <b>목록은 줄어들기만 해야 한다</b> — 새 항목 추가는 새 위반을 승인하는 것이다.
     * 해소 방향은 이 어댑터들을 {@code ..persistence..}가 아닌 별도 위치로 옮기는 것이고, 그 판단은
     * 이 단계(규칙 추가만 하고 프로덕션 코드는 건드리지 않음)의 범위 밖이다.
     */
    private static final Set<String> SEALED_PERSISTENCE_TO_QUERY = Set.of(
        "com.tastyhouse.infrastructure.product.persistence.ProductReviewStatisticsAdapter",
        "com.tastyhouse.infrastructure.rank.persistence.MemberReviewCountAdapter",
        "com.tastyhouse.infrastructure.search.persistence.KeywordCountAdapter"
    );

    @Test
    void persistenceShouldNotDependOnQuery() {
        ArchRule rule = noClasses()
            .that(resideInAPackage("com.tastyhouse.infrastructure..persistence..")
                .and(not(sealed()))
                .as("..persistence.. (봉인 제외)"))
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure..query..")
            .because("write 어댑터는 read model을 의존하지 않는다(read→write 단방향)");

        rule.check(classes);
    }

    /**
     * 봉인 목록이 낡지 않았음을 보장한다(수동 목록을 택할 때의 필수 조건 — {@code ContextBoundaryTest} 선례).
     *
     * <p>목록에 올라 있는 클래스가 실제로는 더 이상 위반하지 않으면(이관·삭제됐으면) 실패시켜, 낡은 항목이
     * 조용히 남아 다른 위반을 가리는 것을 막는다. 목록이 전부 비면 봉인 장치 자체를 제거하고 순수 강제로
     * 전환한다.
     */
    @Test
    void sealedPersistenceToQueryShouldNotBeStale() {
        for (String sealedName : SEALED_PERSISTENCE_TO_QUERY) {
            ArchRule stillViolates = noClasses()
                .that(hasName(sealedName))
                .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure..query..");

            if (!stillViolates.evaluate(classes).hasViolation()) {
                throw new AssertionError(
                    "봉인 목록이 낡았습니다 — 더 이상 위반하지 않으므로 SEALED_PERSISTENCE_TO_QUERY에서 제거하세요: " + sealedName);
            }
        }
    }

    /**
     * 봉인이 전부 해소되면 봉인 장치 자체를 제거하고 순수 강제로 전환하라고 알린다
     * ({@code ContextBoundaryTest#sealedViolationListShouldNotBeEmpty} 선례).
     */
    @Test
    void sealedPersistenceToQueryListShouldNotBeEmpty() {
        if (SEALED_PERSISTENCE_TO_QUERY.isEmpty()) {
            throw new AssertionError(
                "봉인 목록이 비었습니다 — SEALED_PERSISTENCE_TO_QUERY와 짝 테스트를 제거하고 순수 강제로 전환하세요.");
        }
    }

    /**
     * infra가 자체 소유하는 읽기 계약(<b>봉인 목록</b>).
     *
     * <p>읽기 계약은 원칙적으로 응용 계층이 소유하지만, <em>application 소비자가 하나도 없고</em> infra
     * 어댑터·DAO만 소비하는 내부 투영 계약은 계약 모듈을 부풀릴 뿐이므로 infra가 자체 소유한다
     * ({@code ShopNoticeRow} 선례).
     *
     * <p><b>패키지 술어가 아니라 클래스명으로 봉인하는 이유</b>: 모든 QueryDao가 이미
     * {@code com.tastyhouse.infrastructure.<ctx>.query} 패키지에 살기 때문에, 예외를
     * {@code resideInAPackage("com.tastyhouse.infrastructure..query..")}로 표현하면 DAO가 자기 패키지에
     * 인터페이스를 하나 선언하기만 해도 통과한다 — 이 규칙이 원래 잡아야 할 위반("application이 소유해야 할
     * 계약을 infra가 몰래 자기 패키지에 만드는 것")이 그대로 허용 범위가 되어 규칙이 무력해진다. 그래서
     * {@code SEALED_PERSISTENCE_TO_QUERY} 선례대로 FQN으로 명시 제외하며, <b>목록은 줄어들기만 해야 한다</b>.
     */
    private static final Set<String> INFRA_OWNED_QUERY_PORTS = Set.of(
        "com.tastyhouse.infrastructure.review.query.MemberReviewCountQueryPort"
    );

    @Test
    void queryDaosShouldImplementQueryPorts() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("QueryDao")
            .should().implement(
                resideInAPackage("com.tastyhouse.application..port.out..")
                    .or(infraOwnedQueryPort()))
            .because("조회 계약은 응용 계층이 소유하고 DAO가 구현한다. "
                + "단 application 소비자가 없는 내부 투영 계약은 infra가 자체 소유한다(봉인 목록)");

        rule.check(classes);
    }

    /**
     * infra 자체 소유 봉인 목록이 낡지 않았음을 보장한다({@code sealedPersistenceToQueryShouldNotBeStale} 선례).
     *
     * <p>목록의 계약이 사라졌거나(삭제) application 계층으로 되돌아갔으면 실패시킨다. 04장에서 포트 소유자가
     * 확정돼 이 포트가 application으로 올라가면 이 테스트가 자동으로 정리 신호를 띄운다.
     */
    @Test
    void infraOwnedQueryPortListShouldNotBeStale() {
        for (String portName : INFRA_OWNED_QUERY_PORTS) {
            if (!classes.contain(portName)) {
                throw new AssertionError(
                    "봉인 목록이 낡았습니다 — infra에 더 이상 존재하지 않으므로 INFRA_OWNED_QUERY_PORTS에서 제거하세요: "
                        + portName);
            }
        }
    }

    /**
     * 봉인이 전부 해소되면 봉인 장치 자체를 제거하고 순수 강제로 전환하라고 알린다
     * ({@code sealedPersistenceToQueryListShouldNotBeEmpty} 선례).
     */
    @Test
    void infraOwnedQueryPortListShouldNotBeEmpty() {
        if (INFRA_OWNED_QUERY_PORTS.isEmpty()) {
            throw new AssertionError(
                "봉인 목록이 비었습니다 — INFRA_OWNED_QUERY_PORTS와 짝 테스트를 제거하고 "
                    + "queryDaosShouldImplementQueryPorts를 순수 강제로 되돌리세요.");
        }
    }

    private static DescribedPredicate<JavaClass> infraOwnedQueryPort() {
        return DescribedPredicate.describe(
            "infra 자체 소유 읽기 계약",
            javaClass -> INFRA_OWNED_QUERY_PORTS.contains(javaClass.getName()));
    }

    private static DescribedPredicate<JavaClass> sealed() {
        return DescribedPredicate.describe(
            "봉인된 포트 어댑터",
            javaClass -> SEALED_PERSISTENCE_TO_QUERY.contains(javaClass.getName()));
    }

    private static DescribedPredicate<JavaClass> hasName(String fullyQualifiedName) {
        return DescribedPredicate.describe(
            fullyQualifiedName,
            javaClass -> javaClass.getName().equals(fullyQualifiedName));
    }
}
