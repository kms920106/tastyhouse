package com.tastyhouse.application.architecture;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaConstructor;
import com.tngtech.archunit.core.domain.JavaConstructorCall;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScans;
import org.springframework.transaction.event.TransactionalEventListener;

import com.tastyhouse.architecture.ModuleOrigin;
import com.tastyhouse.architecture.UseCaseServices;

import static com.tngtech.archunit.base.DescribedPredicate.not;
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

    private static final int RESOLVED_FLOOR = 43;

    private static final List<String> PERSISTENCE_TECHNOLOGY_PACKAGES = List.of(
        "org.springframework.dao",
        "org.springframework.orm",
        "org.springframework.data",
        "jakarta.persistence",
        "org.apache.ibatis",
        "org.mybatis"
    );

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
            .that(UseCaseServices.commands())
            .should().dependOnClassesThat().haveSimpleNameEndingWith("QueryPort")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("QueryService")
            .orShould().dependOnClassesThat().haveSimpleNameEndingWith("QueryUseCase")
            .because("CommandService는 조회 어댑터도 읽기 포트도 조회 유스케이스도 주입하지 않는다(CQRS 교차 주입 금지)");

        rule.check(classes);
    }

    @Test
    void queryServicesShouldNotDependOnWritePorts() {
        ArchRule rule = noClasses()
            .that(UseCaseServices.queries())
            .and().doNotHaveFullyQualifiedName("com.tastyhouse.application.shop.service.ShopDeliveryTipViewQueryService")
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
    void useCaseServicesShouldImplementExactlyOneUseCase() {
        List<JavaClass> services = useCaseServices();
        List<String> violations = services.stream()
            .filter(service -> portInInterfacesOf(service).size() != 1)
            .map(service -> service.getName() + " → " + portInInterfacesOf(service).stream().map(JavaClass::getSimpleName).toList())
            .toList();

        assertThat(violations)
            .as("유스케이스 서비스는 port.in 인터페이스를 정확히 1개 구현한다(유스케이스 하나 = 포트 하나 = 서비스 하나)")
            .isEmpty();
    }

    @Test
    void useCasesShouldDeclareSingleOperation() {
        List<JavaClass> ports = classes.stream()
            .filter(JavaClass::isInterface)
            .filter(LayerRulesTest::isPortInType)
            .filter(port -> port.getSimpleName().endsWith("UseCase"))
            .filter(port -> !ModuleOrigin.isFrom(port, ModuleOrigin.BATCH))
            .toList();

        assertThat(ports)
            .as("web·admin·ceo의 UseCase 포트가 줄면 출처 판정이 깨져 이 규칙이 공허하게 통과할 수 있다")
            .hasSizeGreaterThanOrEqualTo(582);

        List<String> violations = ports.stream()
            .filter(port -> abstractOperationsOf(port).size() != 1)
            .map(port -> port.getName() + " → " + abstractOperationsOf(port))
            .toList();

        assertThat(violations)
            .as("web·admin·ceo의 UseCase 포트는 연산 1개(추상 메서드 1개)만 선언한다 — 의미가 다른 오버로드도 이름을 나눈 별도 포트로 둔다")
            .isEmpty();
    }

    @Test
    void useCaseServiceNameShouldMatchPort() {
        List<String> violations = useCaseServices().stream()
            .filter(service -> portInInterfacesOf(service).size() == 1)
            .filter(service -> {
                String portName = portInInterfacesOf(service).getFirst().getSimpleName();
                String expected = portName.substring(0, portName.length() - "UseCase".length()) + "Service";
                return !service.getSimpleName().equals(expected);
            })
            .map(service -> service.getSimpleName() + " ↔ " + portInInterfacesOf(service).getFirst().getSimpleName())
            .toList();

        assertThat(violations)
            .as("유스케이스 서비스의 이름은 구현한 포트 이름에서 UseCase를 Service로 바꾼 것이다")
            .isEmpty();
    }

    @Test
    void useCaseServicesShouldHaveSinglePublicOperation() {
        List<String> violations = useCaseServices().stream()
            .filter(service -> publicOperationsOf(service).size() != 1)
            .map(service -> service.getName() + " → " + publicOperationsOf(service))
            .toList();

        assertThat(violations)
            .as("유스케이스 서비스는 생성자를 뺀 public 메서드가 정확히 1개다(공유 로직은 Reader·Validator 또는 record 정적 팩토리로)")
            .isEmpty();
    }

    private List<JavaClass> useCaseServices() {
        List<JavaClass> services = classes.stream()
            .filter(UseCaseServices::isUseCaseService)
            .toList();
        assertThat(services)
            .as("유스케이스 서비스가 줄면 판정 술어가 깨져 이 규칙들이 공허하게 통과할 수 있다")
            .hasSizeGreaterThanOrEqualTo(582);
        return services;
    }

    private static List<JavaClass> portInInterfacesOf(JavaClass service) {
        return service.getRawInterfaces().stream()
            .filter(LayerRulesTest::isPortInType)
            .toList();
    }

    private static List<String> abstractOperationsOf(JavaClass port) {
        return port.getMethods().stream()
            .filter(method -> method.getModifiers().contains(JavaModifier.ABSTRACT))
            .map(JavaMethod::getName)
            .toList();
    }

    private static List<String> publicOperationsOf(JavaClass service) {
        return service.getMethods().stream()
            .filter(method -> method.getModifiers().contains(JavaModifier.PUBLIC))
            .filter(method -> !method.getModifiers().contains(JavaModifier.STATIC))
            .filter(method -> !method.getModifiers().contains(JavaModifier.SYNTHETIC))
            .filter(method -> !method.getModifiers().contains(JavaModifier.BRIDGE))
            .map(JavaMethod::getName)
            .toList();
    }

    @Test
    void commandRecordsShouldBeBoundaryTyped() {
        ArchRule rule = noClasses()
            .that().resideInAPackage("..port.in..")
            .and(not(ModuleOrigin.from(ModuleOrigin.BATCH)))
            .should().dependOnClassesThat(
                resideInAnyPackage(
                    "com.tastyhouse.domain..",
                    "com.tastyhouse.infrastructure..",
                    "org.springframework.web.."
                ).and(not(resideInAPackage("org.springframework.web.multipart..")))
            )
            .because("Command는 도메인 모델·infra·web 타입을 싣지 않는다"
                + "(구조적 가드는 application의 ApplicationException·ApplicationErrorCode로 던진다)");

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
            .that(UseCaseServices.commands())
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
    void applicationShouldNotDependOnPersistenceTechnology() {
        ArchRule rule = noClasses()
            .should().dependOnClassesThat().resideInAnyPackage(
                PERSISTENCE_TECHNOLOGY_PACKAGES.stream().map(name -> name + "..").toArray(String[]::new))
            .because("영속 기술의 예외·타입은 어댑터가 application/shared/port/out의 포트 예외로 번역한다 — 포트 계약은 JPA·MyBatis 어느 구현으로도 바뀔 수 있어야 한다");

        rule.check(classes);

        List<String> caughtViolations = classes.stream()
            .flatMap(javaClass -> javaClass.getCodeUnits().stream())
            .flatMap(codeUnit -> codeUnit.getTryCatchBlocks().stream()
                .flatMap(block -> block.getCaughtThrowables().stream())
                .filter(caught -> PERSISTENCE_TECHNOLOGY_PACKAGES.stream().anyMatch(caught.getPackageName()::startsWith))
                .map(caught -> codeUnit.getFullName() + " catches " + caught.getName()))
            .sorted()
            .toList();

        assertThat(caughtViolations)
            .as("catch 절의 예외 타입은 ArchUnit 의존 그래프에 잡히지 않으므로 try-catch 블록을 직접 검사한다")
            .isEmpty();
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
    void listenersAndConfigsShouldResideInCore() {
        List<JavaClass> listeners = classes.stream().filter(DECLARE_TRANSACTIONAL_EVENT_LISTENER).toList();
        List<JavaClass> configurations = classes.stream()
            .filter(javaClass -> javaClass.isAnnotatedWith(CONFIGURATION))
            .toList();

        assertThat(listeners)
            .as("리스너가 12개 미만이면 이 규칙이 대상을 잃었다")
            .hasSizeGreaterThanOrEqualTo(12);
        assertThat(configurations)
            .as("설정 클래스가 0개면 이 규칙이 공허하게 통과한다")
            .isNotEmpty();

        List<String> violations = new ArrayList<>();
        for (JavaClass listener : listeners) {
            if (!ModuleOrigin.isFrom(listener, ModuleOrigin.CORE)
                || !(listener.getPackageName().endsWith(".listener") || listener.getPackageName().contains(".listener."))) {
                violations.add(listener.getName() + ": " + ModuleOrigin.of(listener));
            }
        }
        for (JavaClass configuration : configurations) {
            if (!ModuleOrigin.isFrom(configuration, ModuleOrigin.CORE)) {
                violations.add(configuration.getName() + ": " + ModuleOrigin.of(configuration));
            }
        }

        assertThat(violations)
            .as("AFTER_COMMIT 리스너와 @Configuration은 4앱 전부에 떠야 하므로 core(application 모듈)의 ..listener..·설정에만 둔다 "
                + "— 앱 모듈에 두면 그 앱에서만 떠서 다른 앱이 발행한 이벤트의 후속 처리가 예외도 로그도 없이 유실된다")
            .isEmpty();
    }

    @Test
    void coreShouldNotContainUseCasesOrOrchestrators() {
        List<JavaClass> coreClasses = classes.stream()
            .filter(javaClass -> ModuleOrigin.isFrom(javaClass, ModuleOrigin.CORE))
            .toList();

        assertThat(coreClasses)
            .as("core 출처 클래스가 줄면 출처 판정이 깨져 이 규칙이 공허하게 통과할 수 있다")
            .hasSizeGreaterThanOrEqualTo(500);

        List<String> violations = coreClasses.stream()
            .filter(javaClass -> isPortInType(javaClass)
                || (!javaClass.isInterface() && implementsPortInInterface(javaClass)))
            .map(JavaClass::getName)
            .toList();

        assertThat(violations)
            .as("core는 4앱 전부에 실리므로 UseCase·Command(..port.in..)·UseCase 구현(유스케이스 서비스)을 두지 않는다 "
                + "— 그 앱의 {app}-application 모듈로 옮긴다")
            .isEmpty();
    }

    @Test
    void coreBeansShouldOnlyDependOnCoreVisibleTypes() {
        List<JavaClass> beans = classes.stream()
            .filter(javaClass -> !javaClass.isInterface())
            .filter(javaClass -> javaClass.isAnnotatedWith(SERVICE) || javaClass.isAnnotatedWith(COMPONENT))
            .toList();
        List<JavaMethod> beanMethods = classes.stream()
            .filter(javaClass -> javaClass.isAnnotatedWith(CONFIGURATION))
            .flatMap(javaClass -> javaClass.getMethods().stream())
            .filter(method -> method.isAnnotatedWith(BEAN))
            .toList();

        Set<String> violations = new TreeSet<>();
        int checked = 0;
        int resolved = 0;
        for (JavaClass bean : beans) {
            String beanModule = ModuleOrigin.of(bean);
            List<String> hostApps = beanModule.equals(ModuleOrigin.CORE) ? ModuleOrigin.APP_MODULES : List.of(beanModule);
            for (JavaClass dependency : constructorParameterTypes(bean)) {
                if (!isApplicationInterface(dependency)) {
                    continue;
                }
                checked++;
                List<JavaClass> candidates = candidatesOf(dependency, beans, beanMethods);
                if (candidates.isEmpty()) {
                    continue;
                }
                resolved++;
                for (String appModule : hostApps) {
                    long visible = candidates.stream()
                        .filter(candidate -> ModuleOrigin.isFrom(candidate, ModuleOrigin.CORE)
                            || ModuleOrigin.isFrom(candidate, appModule))
                        .count();
                    if (visible == 0) {
                        violations.add(bean.getName() + " → " + dependency.getName()
                            + ": " + appModule + " 컨텍스트에 구현이 없다(다른 앱 모듈에만 있다)");
                    } else if (visible >= 2) {
                        violations.add(bean.getName() + " → " + dependency.getName()
                            + ": " + appModule + " 컨텍스트에 주입 후보가 " + visible + "개다");
                    }
                }
            }
        }

        assertThat(checked)
            .as("검사한 인터페이스 의존이 줄면 타입 해석이 깨져 이 규칙이 공허하게 통과할 수 있다")
            .isGreaterThanOrEqualTo(300);
        assertThat(resolved)
            .as("application 안에서 구현 후보가 해석된 의존이 줄면 후보 탐색이 깨져 이 규칙이 공허하게 통과할 수 있다")
            .isGreaterThanOrEqualTo(RESOLVED_FLOOR);
        assertThat(violations)
            .as("빈은 자기가 뜨는 앱 컨텍스트(core 빈은 4앱 전부, 앱 빈은 그 앱)에서 application 인터페이스 구현을 정확히 1개 봐야 한다 — "
                + "구현이 다른 앱 모듈에만 있으면 그 앱 기동이 실패하고, 2개 이상이면 주입이 모호해진다")
            .isEmpty();
    }

    @Test
    void configurationsShouldNotRegisterStereotypedClasses() {
        List<String> violations = new ArrayList<>();
        for (JavaClass configuration : classes) {
            if (!configuration.isAnnotatedWith(CONFIGURATION)) {
                continue;
            }
            if (configuration.isAnnotatedWith(COMPONENT) || configuration.isAnnotatedWith(SERVICE)) {
                violations.add(configuration.getName() + ": 설정 클래스가 @Component/@Service를 겸한다");
            }
            for (JavaMethod method : configuration.getMethods()) {
                if (method.isAnnotatedWith(BEAN) && isStereotyped(method.getRawReturnType())) {
                    violations.add(configuration.getName() + "#" + method.getName()
                        + ": @Bean 반환 타입 " + method.getRawReturnType().getName() + "이 스캔 대상이다");
                }
            }
            for (JavaConstructorCall call : configuration.getConstructorCallsFromSelf()) {
                JavaClass target = call.getTargetOwner();
                if (!target.equals(configuration) && isStereotyped(target)) {
                    violations.add(configuration.getName() + ": 스캔 대상인 " + target.getName() + "를 직접 생성한다");
                }
            }
        }

        assertThat(violations)
            .as("@Configuration은 스테레오타입 없는 POJO(domain 계산기 등)만 @Bean으로 등록한다 — @Service/@Component 클래스를 "
                + "생성하면 스캔과 @Bean이 겹친다(인터페이스로 반환해도 생성 호출로 잡는다)")
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
                + "도메인 타입 협력이 필요하면 write 포트나 UseCase를 구현하지 않는 도메인 서비스를 직접 주입한다")
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

    @Test
    void useCaseImplementationsShouldNotBePublic() {
        List<JavaClass> useCaseImplementations = classes.stream()
            .filter(javaClass -> !javaClass.isInterface())
            .filter(javaClass -> javaClass.getEnclosingClass().isEmpty())
            .filter(LayerRulesTest::implementsPortInInterface)
            .toList();

        assertThat(useCaseImplementations)
            .as("UseCase 구현 클래스 집합이 비면 이 규칙은 공허하게 통과한다")
            .hasSizeGreaterThanOrEqualTo(200);

        List<String> violations = useCaseImplementations.stream()
            .filter(javaClass -> javaClass.getModifiers().contains(JavaModifier.PUBLIC))
            .map(JavaClass::getName)
            .toList();

        assertThat(violations)
            .as("UseCase 구현 서비스는 port.in 인터페이스로만 주입되므로 public이 아니다 — "
                + "다른 패키지가 구체 타입을 주입하려 하면 컴파일 에러가 나게 한다")
            .isEmpty();
    }

    @Test
    void listenersAndConfigsShouldNotBePublic() {
        ArchRule rule = classes()
            .that(DescribedPredicate.describe(
                "..listener.. 의 클래스 또는 @Configuration",
                javaClass -> javaClass.getPackageName().endsWith(".listener") || javaClass.isAnnotatedWith(CONFIGURATION)))
            .and().areTopLevelClasses()
            .should().notBePublic()
            .because("도메인 이벤트 리스너와 @Configuration은 컴포넌트 스캔으로만 등록되고 어떤 클래스도 직접 참조하지 않는다");

        rule.check(classes);
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

    private static boolean isPortInType(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return packageName.endsWith(".port.in") || packageName.contains(".port.in.");
    }

    private static boolean isApplicationInterface(JavaClass javaClass) {
        return javaClass.isInterface() && javaClass.getName().startsWith("com.tastyhouse.application.");
    }

    private static boolean isStereotyped(JavaClass javaClass) {
        return javaClass.isAnnotatedWith(SERVICE) || javaClass.isAnnotatedWith(COMPONENT);
    }

    private static List<JavaClass> constructorParameterTypes(JavaClass javaClass) {
        return javaClass.getConstructors().stream()
            .map(JavaConstructor::getRawParameterTypes)
            .flatMap(List::stream)
            .toList();
    }

    private static List<JavaClass> candidatesOf(JavaClass dependency, List<JavaClass> beans, List<JavaMethod> beanMethods) {
        List<JavaClass> candidates = new ArrayList<>();
        for (JavaClass bean : beans) {
            if (!bean.getModifiers().contains(JavaModifier.ABSTRACT)
                && bean.getAllRawInterfaces().contains(dependency)
                && constructorParameterTypes(bean).stream().noneMatch(dependency::equals)) {
                candidates.add(bean);
            }
        }
        for (JavaMethod method : beanMethods) {
            JavaClass returnType = method.getRawReturnType();
            if (returnType.equals(dependency) || returnType.getAllRawInterfaces().contains(dependency)) {
                candidates.add(method.getOwner());
            }
        }
        return candidates;
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
