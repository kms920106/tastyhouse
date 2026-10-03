package com.tastyhouse.application.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shared.marker.WebApp;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.core.domain.properties.CanBeAnnotated.Predicates.annotatedWith;
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
    void commandServicesShouldNotDependOnQueryPorts() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("CommandService")
            .should().dependOnClassesThat().haveSimpleNameEndingWith("QueryPort")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("QueryService")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("QueryUseCase")
            .because("CommandService는 조회 어댑터도 읽기 포트도 조회 유스케이스도 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    @Test
    void queryServicesShouldNotDependOnWritePorts() {
        ArchRule rule = noClasses()
            .that().haveSimpleNameEndingWith("QueryService")
            .and().doNotHaveFullyQualifiedName("com.tastyhouse.application.shop.service.ShopQueryService")
            .and().doNotHaveFullyQualifiedName("com.tastyhouse.application.admin.service.AdminQueryService")
            .and().doNotHaveFullyQualifiedName("com.tastyhouse.application.ceo.service.CeoOwnerQueryService")
            .should().dependOnClassesThat().resideInAPackage("com.tastyhouse.application..port.out.write..")
            .because("QueryService는 write 포트(도메인 타입 리포지토리)를 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    @Test
    void writePortsShouldBeNamedPort() {
        ArchRule rule = classes()
            .that().resideInAPackage("com.tastyhouse.application..port.out.write..")
            .and().areInterfaces()
            .should().haveSimpleNameEndingWith("Port")
            .because("쓰기 포트는 XxxPersistencePort로 짓는다 — 옛 XxxRepository 이름은 RuleAnchorTest#writePortsExist와 "
                + "컨트롤러 가드의 이름 기반 대상에서 빠진다");

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
            .that(resideInAPackage("com.tastyhouse.application..port.out..")
                .and(not(resideInAPackage("com.tastyhouse.application..port.out.write..")))
                .as("..port.out.. (port.out.write 제외)"))
            .should().onlyDependOnClassesThat(resideInAPackage("java..")
                .or(resideInAPackage("com.tastyhouse.application..port.out..")
                    .and(not(resideInAPackage("com.tastyhouse.application..port.out.write.."))))
                .as("java.. 또는 port.out.. (port.out.write 제외)"))
            .because("읽기 계약은 조회 DAO가 구현한다 — 도메인 타입을 참조하면 조회 DAO가 domain을 알게 된다");

        rule.check(classes);
    }

    @Test
    void writePortsShouldOnlyDependOnDomainAndPortOut() {
        ArchRule rule = classes()
            .that().resideInAPackage("com.tastyhouse.application..port.out.write..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(
                "java..",
                "com.tastyhouse.domain..",
                "com.tastyhouse.application..port.out.."
            )
            .because("write 포트는 도메인 모델을 주고받는 리포지토리 계약이다 — persistence가 도메인 타입으로 직접 구현한다");

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
        DescribedPredicate<JavaClass> sharedDomainService = new DescribedPredicate<>("마커만 단 도메인 서비스") {
            @Override
            public boolean test(JavaClass javaClass) {
                return isMarkerOnlyClass(javaClass) && isDomainServiceLocation(javaClass);
            }
        };

        ArchRule rule = classes()
            .that().areAnnotatedWith(SharedApp.class)
            .should(ArchCondition.from(sharedListener.or(sharedConfiguration).or(sharedDomainService)
                .as("..listener..의 @TransactionalEventListener 클래스이거나 ..config..의 @Configuration 클래스이거나 "
                    + "..service..의 스테레오타입 없는 도메인 서비스(*CommandService/*QueryService·UseCase 구현 제외)")))
            .because("@SharedApp은 리스너·설정 클래스·여러 앱이 공유하는 도메인 서비스 전용이다 — 앱 오케스트레이터(@Service)에 "
                + "붙이면 앱 격리(앱 마커 스캔 필터)를 우회한다");

        rule.check(classes);
    }

    @Test
    void markerOnlyClassesShouldBeDomainServices() {
        List<JavaClass> markerOnlyClasses = classes.stream()
            .filter(LayerRulesTest::isMarkerOnlyClass)
            .toList();

        assertThat(markerOnlyClasses)
            .as("스테레오타입 없이 앱 마커만 단 클래스가 0개면 이 규칙이 공허하게 통과한다")
            .hasSizeGreaterThanOrEqualTo(78);

        List<String> violations = markerOnlyClasses.stream()
            .filter(javaClass -> !isDomainServiceLocation(javaClass))
            .map(JavaClass::getName)
            .toList();

        assertThat(violations)
            .as("스테레오타입 없이 앱 마커만 다는 형태는 ..service..의 도메인 서비스 전용이다(*CommandService/*QueryService와 "
                + "UseCase 구현은 @Service를 단다) — 리스너는 @Component, 설정은 @Configuration을 함께 단다")
            .isEmpty();
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

    @Test
    void servicesShouldDependOnUseCasesNotImplementations() {
        Set<JavaClass> useCaseImplementations = classes.stream()
            .filter(javaClass -> !javaClass.isInterface())
            .filter(LayerRulesTest::implementsPortInInterface)
            .collect(Collectors.toSet());

        assertThat(useCaseImplementations)
            .as("UseCase 구현 클래스 집합이 비면 이 규칙은 공허하게 통과한다")
            .hasSizeGreaterThanOrEqualTo(200);

        List<String> violations = new ArrayList<>();
        for (JavaClass origin : classes) {
            JavaClass originTop = topLevelOf(origin);
            for (Dependency dependency : origin.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                if (useCaseImplementations.contains(target) && !target.equals(originTop)) {
                    violations.add(dependency.getDescription());
                }
            }
        }

        assertThat(violations)
            .as("UseCase를 구현한 서비스는 구체 타입이 아니라 port.in 인터페이스로 주입한다 — "
                + "도메인 타입 협력이 필요하면 write 포트나 마커 없는 도메인 서비스를 직접 주입한다")
            .isEmpty();
    }

    @Test
    void useCaseFieldsShouldBeNamedUseCase() {
        List<JavaField> useCaseFields = classes.stream()
            .flatMap(javaClass -> javaClass.getFields().stream())
            .filter(field -> isPortInInterface(field.getRawType()))
            .toList();

        assertThat(useCaseFields)
            .as("UseCase 타입 필드가 줄면 타입 해석이 깨져 이 규칙이 공허하게 통과할 수 있다")
            .hasSizeGreaterThanOrEqualTo(15);

        List<String> violations = useCaseFields.stream()
            .filter(field -> !field.getName().endsWith("UseCase"))
            .map(JavaField::getFullName)
            .toList();

        assertThat(violations)
            .as("port.in 인터페이스 타입 필드는 이름이 UseCase로 끝난다(변수명은 타입을 따른다)")
            .isEmpty();
    }

    private static boolean implementsPortInInterface(JavaClass javaClass) {
        return javaClass.getAllRawInterfaces().stream().anyMatch(LayerRulesTest::isPortInInterface);
    }

    private static boolean isPortInInterface(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return javaClass.isInterface()
            && (packageName.endsWith(".port.in") || packageName.contains(".port.in."));
    }

    private static JavaClass topLevelOf(JavaClass javaClass) {
        JavaClass current = javaClass;
        while (current.getEnclosingClass().isPresent()) {
            current = current.getEnclosingClass().get();
        }
        return current;
    }

    private static boolean isMarkerOnlyClass(JavaClass javaClass) {
        return !javaClass.isInterface()
            && !javaClass.isAnnotation()
            && !javaClass.isAnnotatedWith(SERVICE)
            && !javaClass.isAnnotatedWith(COMPONENT)
            && !javaClass.isAnnotatedWith(CONFIGURATION)
            && hasAppMarker(javaClass);
    }

    private static boolean isDomainServiceLocation(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        String simpleName = javaClass.getSimpleName();
        return (packageName.endsWith(".service") || packageName.contains(".service."))
            && !simpleName.endsWith("CommandService")
            && !simpleName.endsWith("QueryService")
            && !implementsPortInInterface(javaClass);
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

    @Test
    void applicationShouldNotDeclareComponentScan() {
        ArchRule rule = noClasses()
            .should().beMetaAnnotatedWith(ComponentScan.class)
            .orShould().beMetaAnnotatedWith(ComponentScans.class)
            .because("앱별 스캔 범위는 각 앱 부트스트랩의 ApplicationLayerScanConfig가 소유한다 — application은 앱 조립을 모른다");

        rule.check(classes);
    }

}
