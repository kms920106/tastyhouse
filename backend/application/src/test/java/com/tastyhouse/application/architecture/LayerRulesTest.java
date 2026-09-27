package com.tastyhouse.application.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shared.marker.WebApp;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class LayerRulesTest {

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    private static final String CONFIGURATION = "org.springframework.context.annotation.Configuration";

    private static final String BEAN = "org.springframework.context.annotation.Bean";

    private static final String COMPONENT = "org.springframework.stereotype.Component";

    private static final String SERVICE = "org.springframework.stereotype.Service";

    private static final List<Class<? extends Annotation>> APP_MARKERS =
        List.of(WebApp.class, AdminApp.class, CeoApp.class, BatchApp.class, SharedApp.class);

    private static final DescribedPredicate<JavaClass> DECLARE_TRANSACTIONAL_EVENT_LISTENER =
        new DescribedPredicate<>("@TransactionalEventListener 메서드를 가진 클래스") {
            @Override
            public boolean test(JavaClass javaClass) {
                return javaClass.getMethods().stream()
                    .anyMatch(method -> method.isAnnotatedWith(TransactionalEventListener.class));
            }
        };

    @Test
    void commandServicesShouldNotDependOnQueryDaos() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("CommandService")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("QueryPort")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("QueryService")
            .because("CommandService는 조회 어댑터도 읽기 포트도 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    @Test
    void queryServicesShouldNotDependOnWritePorts() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("QueryService")
            .and().doNotHaveFullyQualifiedName("com.tastyhouse.application.shop.service.ShopQueryService")
            .and().doNotHaveFullyQualifiedName("com.tastyhouse.application.admin.service.AdminQueryService")
            .and().doNotHaveFullyQualifiedName("com.tastyhouse.application.ceo.service.CeoOwnerQueryService")
            .should().dependOnClassesThat().resideInAnyPackage("com.tastyhouse.domain..repository..")
            .because("QueryService는 write 포트를 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    @Test
    void commandServicesShouldImplementUseCase() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("CommandService")
            .should().implement(resideInAPackage("..port.in.."))
            .because("CommandService는 대응 CommandUseCase를 구현한다");

        rule.check(classes);
    }

    @Test
    void queryServicesShouldImplementUseCase() {
        ArchRule rule = classes()
            .that().haveSimpleNameEndingWith("QueryService")
            .should().implement(resideInAPackage("..port.in.."))
            .because("QueryService는 대응 QueryUseCase를 구현한다");

        rule.check(classes);
    }

    @Test
    void commandRecordsShouldBeBoundaryTyped() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..port.in..")
            .and().areNotAnnotatedWith(BatchApp.class)
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "com.tastyhouse.domain..",
                    "com.tastyhouse.infrastructure..",
                    "org.springframework.web.."
                ).and(not(resideInAPackage("com.tastyhouse.domain.exception..")))
                 .and(not(resideInAPackage("org.springframework.web.multipart..")))
            )
            .because("Command는 도메인 모델·infra·web 타입을 싣지 않는다"
                + "(에러 계약은 횡단 관심사라 예외)");

        rule.check(classes);
    }

    @Test
    void commandRecordsShouldNotHoldMultipartFile() {
        ArchRule rule = fields()
            .that().areDeclaredInClassesThat().resideInAPackage("..port.in..")
            .should().notHaveRawType("org.springframework.web.multipart.MultipartFile")
            .because("Command 필드로 업로드 타입을 싣지 않는다(업로드는 UseCase 메서드의 별도 파라미터)");

        rule.check(classes);
    }

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

    @Test
    void commandServicesShouldNotDependOnRequestRecords() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("CommandService")
            .should().dependOnClassesThat().resideInAnyPackage("..request..")
            .because("CommandService는 Request record를 받지 않는다(매핑은 컨트롤러가 소유)");

        rule.check(classes);
    }

    @Test
    void shouldNotDependOnQuerydsl() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.querydsl..");

        rule.check(classes);
    }

    @Test
    void shouldNotDependOnInfrastructure() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.infrastructure..");

        rule.check(classes);
    }

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

        rule.check(classes);
    }

    @Test
    void applicationShouldNotDependOnSwagger() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("io.swagger..")
            .because("유스케이스 계층은 API 문서화 도구를 알지 않는다(Response 조립은 각 api 모듈 담당)");

        rule.check(classes);
    }

    @Test
    void listenersShouldBeShared() {
        ArchRule rule = classes()
            .that(DECLARE_TRANSACTIONAL_EVENT_LISTENER)
            .should().beAnnotatedWith(SharedApp.class)
            .andShould().resideInAPackage("..listener..")
            .because("마커 없는 AFTER_COMMIT 리스너는 어느 앱에도 뜨지 않아 이벤트가 예외도 로그도 없이 유실된다");

        rule.check(classes);
    }

    @Test
    void sharedAppOnlyOnListeners() {
        DescribedPredicate<JavaClass> sharedListener = resideInAPackage("..listener..")
            .and(DECLARE_TRANSACTIONAL_EVENT_LISTENER);
        DescribedPredicate<JavaClass> sharedConfiguration = resideInAPackage("..config..")
            .and(annotatedWith(CONFIGURATION));

        ArchRule rule = classes()
            .that().areAnnotatedWith(SharedApp.class)
            .should(ArchCondition.from(sharedListener.or(sharedConfiguration)
                .as("..listener..의 @TransactionalEventListener 클래스이거나 ..config..의 @Configuration 클래스")))
            .because("@SharedApp은 리스너와 설정 클래스 전용이다 — 일반 빈에 붙이면 앱 격리(앱 마커 스캔 필터)를 우회한다");

        rule.check(classes);
    }

    @Test
    void sharedConfigsShouldOnlyDeclareUnmarkedBeans() {
        List<String> violations = new ArrayList<>();
        for (JavaClass configuration : classes) {
            if (!configuration.isAnnotatedWith(SharedApp.class)
                || !configuration.isAnnotatedWith(CONFIGURATION)) {
                continue;
            }
            if (configuration.isAnnotatedWith(COMPONENT) || configuration.isAnnotatedWith(SERVICE)) {
                violations.add(configuration.getName() + ": @SharedApp 설정 클래스가 @Component/@Service를 겸한다");
            }
            for (JavaMethod method : configuration.getMethods()) {
                if (!method.isAnnotatedWith(BEAN)) {
                    continue;
                }
                JavaClass beanType = method.getRawReturnType();
                if (hasAppMarker(beanType)) {
                    violations.add(configuration.getName() + "#" + method.getName()
                        + ": @Bean 반환 타입 " + beanType.getName() + "에 앱 마커가 있다");
                }
            }
            for (JavaConstructorCall call : configuration.getConstructorCallsFromSelf()) {
                JavaClass target = call.getTargetOwner();
                if (!target.equals(configuration) && hasAppMarker(target)) {
                    violations.add(configuration.getName() + ": 앱 마커가 있는 "
                        + target.getName() + "를 직접 생성한다");
                }
            }
        }

        assertThat(violations)
            .as("@SharedApp 설정 클래스는 마커 없는 POJO만 @Bean으로 등록한다 — 마커가 붙은 클래스를 "
                + "생성하면 스캔과 @Bean이 겹치거나 앱 격리를 우회한다(인터페이스로 반환해도 생성 호출로 잡는다)")
            .isEmpty();
    }

    private static boolean hasAppMarker(JavaClass javaClass) {
        return APP_MARKERS.stream().anyMatch(javaClass::isAnnotatedWith);
    }

    @Test
    void applicationShouldNotDependOnApiCommon() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.apicommon..")
            .because("유스케이스 계층은 표현 모듈(api-common-module)을 알지 않는다");

        rule.check(classes);
    }

}
