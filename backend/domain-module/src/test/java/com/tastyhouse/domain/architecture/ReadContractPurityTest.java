package com.tastyhouse.domain.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

/**
 * 이 모듈이 소유하는 <b>공유 읽기 계약</b>의 순수성 규칙(ArchUnit).
 *
 * <p><b>왜 {@link DomainPurityTest}와 별도 파일인가</b>: 챕터 05~06으로 2개 이상의 앱이 함께 쓰는
 * 읽기 계약이 이 모듈로 왔지만, 그 패키지는 {@code com.tastyhouse.application.<ctx>.port.out}
 * 그대로다(split package — 소비 측 import를 바꾸지 않기 위한 의도된 선택). 반면
 * {@code DomainPurityTest}·{@code ContextBoundaryTest}는 대상을 {@code importPackages("com.tastyhouse.domain")}
 * 으로 모으므로 이 계약들은 <b>두 테스트의 스캔 범위 밖</b>에 있다. 그것이 의도된 구분이다 —
 * 읽기 계약은 도메인 모델이 아니라 표현용 투영이라 컨텍스트 경계 규칙(model/repository/service
 * 상호 참조 금지)의 대상이어서는 안 된다. 다만 순수성만은 별도로 지켜야 하므로 이 파일이 맡는다.
 *
 * <p>이 모듈에서는 프레임워크 차단 자체는 여전히 <b>컴파일 게이트</b>가 1차로 담당한다 — 루트
 * {@code build.gradle}이 domain-module을 spring 주입 {@code subprojects} 블록에서 제외하므로
 * {@code import org.springframework...} 한 줄이 실제 컴파일 에러다. 따라서 이 규칙의 실질적 역할은
 * <b>2차 방어선</b>이다: 계약이 {@code domain..model}의 애그리거트나 리포지토리를 끌어다 쓰는 것을 막는다.
 * 그것이 허용되면 표현용 투영이 도메인 내부 구조에 결합되어, 이 계약들을 domain-module에 둘 수 있었던
 * 근거("도메인 enum·원시타입만 담는 도메인 개념")가 무너진다.
 *
 * <p>{@code allowEmptyShould(true)}는 쓰지 않는다. 이 모듈이 공유 계약을 실제로 소유하므로 대상 0건이면
 * 그 자체가 실패로 드러나야 한다.
 */
class ReadContractPurityTest {

    private final JavaClasses readContracts = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    /**
     * 공유 읽기 계약은 프레임워크·인프라를 알지 않는다.
     *
     * <p>허용 대상을 {@code java..}·{@code com.tastyhouse.domain..}과 자기 자신으로 한정한다.
     * 앱별 {@code {앱}-application} 모듈의 동명 규칙과 같은 형태이며, 계약이 어느 모듈로 가든
     * 동일한 순수성이 적용되도록 5개 모듈 전부에 같은 규칙을 둔다.
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
}
