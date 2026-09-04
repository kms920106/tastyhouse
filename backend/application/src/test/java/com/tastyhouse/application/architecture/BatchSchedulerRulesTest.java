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
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * batch 앱 고유 규칙 4종 + exact anchor 3종.
 *
 * <p>batch는 나머지 세 앱과 모양이 다르다 — CQRS 분리를 쓰지 않아 {@code *CommandService}·
 * {@code *QueryService}가 0개이고 잡 본문을 {@code *SchedulerService}에 담으며, 스케줄이 유일한
 * 입력이라 Command record가 없고 인바운드 포트가 전부 {@code void foo()}다. 그래서 batch에는
 * {@link LayerRulesTest}의 공통 규칙으로 표현되지 않는 규칙이 따로 필요하고, 반대로 공통 규칙보다
 * <b>엄격한</b> 판을 쓸 수 있는 것도 있다.
 *
 * <p>importer를 {@code com.tastyhouse.batchapplication} 하나로 좁히는 이유가 그것이다. 특히
 * {@code inboundPortsShouldBeBoundaryTyped}는 carve-out이 {@code domain.exception..} 하나뿐인
 * 엄격판인데, 다른 앱을 함께 넣으면 그 앱들이 정당하게 쓰는 페이징·업로드 타입에 걸려 실패한다.
 *
 * <p>기존 {@code batch-application}의 {@code LayerRulesTest}·{@code RuleAnchorTest}에서 그대로
 * 옮겨 왔다. batch에도 있던 나머지 규칙(QueryDSL·infra·servlet-free·adapter 역참조·읽기 계약
 * 프레임워크-프리)은 공통 16종과 동일해 {@link LayerRulesTest}에 흡수됐다.
 *
 * <p>anchor 3종은 batch가 규모가 작아 <b>정확히 일치</b>로 둔다(다른 앱은 하한). 잡이 늘거나 줄면
 * 이 숫자를 의식적으로 고치게 되는 것이 의도다. 챕터 03에서 마커 애노테이션 기준으로 재정의한다.
 */
class BatchSchedulerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.batchapplication");

    /**
     * 잡 서비스는 HTTP 계층을 알지 않는다.
     *
     * <p>batch는 CQRS 분리를 쓰지 않아 잡 본문을 {@code *SchedulerService}에 담으므로, 세 이름을 모두
     * 대상으로 잡아 실재하는 잡 서비스(Grade/Rank/Product/AdminDong/SearchKeyword/ReviewBlind/
     * ProductSoldOutRelease)에 anchor 한다.
     *
     * <p>{@link LayerRulesTest#applicationMustBeServletFree()}가 모듈 전체를 더 넓게 막지만, 이 규칙은
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
     * 인바운드 포트({@code ..port.in..})는 경계 타입만 노출한다 — <b>carve-out 1건뿐인 엄격판</b>.
     *
     * <p>배치 잡은 스케줄이 유일한 입력이라 Command record를 두지 않으므로 대상은 UseCase 인터페이스
     * 7종이고, 이들이 도메인 모델·infra·web 타입을 시그니처에 드러내지 않아야 스케줄러(adapter)가
     * 그 타입들을 함께 보지 않게 된다.
     *
     * <p>{@code com.tastyhouse.domain.exception}만 carve-out 으로 허용한다 — 예외는 횡단 관심사라
     * 계층 칸이 없다. web·admin·ceo가 쓰는 페이징·업로드 carve-out 2건은 여기 <b>없다</b>: batch에는
     * 목록 조회도 파일 업로드도 없어 느슨하게 할 이유가 없다. 이것이 이 테스트를 batch importer로
     * 분리해 둔 이유다.
     *
     * <p><b>주의 — 지금은 검사할 표면이 없다.</b> UseCase 7개가 전부 파라미터·반환값 없는
     * {@code void foo()} 하나뿐이라, 의존 그래프에 잡힐 타입 자체가 0건이다(아래
     * {@code inboundPortsExist}가 세는 것은 "인터페이스가 존재함"이지 "검사 대상이 있음"이 아니다).
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
     * Response record는 domain-free·infra-free 순수 데이터 홀더다.
     *
     * <p>대상은 {@code crawling/bbq/response/}의 record 4종({@code BbqProductResponse}·
     * {@code BbqProductCategoryResponse}·{@code BbqProductSubOptionResponse}·
     * {@code SubOptionItemDetailResponse})으로 공허하지 않다.
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
     * {@code *SchedulerService}는 인바운드 포트를 최소 1개 구현한다.
     *
     * <p>{@link LayerRulesTest}의 {@code commandServicesShouldImplementUseCase}·
     * {@code queryServicesShouldImplementUseCase}에 대응하는 batch판이다.
     */
    @Test
    void schedulerServicesShouldImplementUseCase() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("SchedulerService")
            .should().implement(resideInAPackage("..port.in.."))
            .because("SchedulerService는 대응 잡 UseCase를 구현한다");

        rule.check(classes);
    }

    /** {@code applicationServicesShouldNotDependOnWebLayer} · {@code schedulerServicesShouldImplementUseCase}의 anchor. */
    @Test
    void schedulerServicesExist() {
        assertThat(classes.stream()
            .filter(c -> c.getSimpleName().endsWith("SchedulerService"))
            .count())
            .as("*SchedulerService가 0건이면 두 규칙이 공허하게 통과한다")
            .isEqualTo(7);
    }

    /** {@code inboundPortsShouldBeBoundaryTyped}의 anchor. */
    @Test
    void inboundPortsExist() {
        assertThat(classes.stream()
            .filter(c -> resideInAPackage("..port.in..").test(c))
            .count())
            .as("..port.in..이 0건이면 경계 타입 규칙이 공허하게 통과한다")
            .isEqualTo(7);
    }

    /** {@code responseRecordsShouldBeDomainAndInfraFree}의 anchor. */
    @Test
    void responseRecordsExist() {
        assertThat(classes.stream()
            .filter(c -> resideInAnyPackage("..request..", "..response..").test(c))
            .count())
            .as("crawling/bbq/response record가 0건이면 record 규칙이 공허하게 통과한다")
            .isEqualTo(4);
    }

}
