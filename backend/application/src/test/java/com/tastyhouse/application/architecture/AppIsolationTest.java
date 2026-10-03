package com.tastyhouse.application.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.domain.JavaParameterizedType;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.tastyhouse.application.auth.port.out.SocialOAuthClient;
import com.tastyhouse.application.crawling.bbq.port.out.BbqMenuPort;
import com.tastyhouse.application.crawling.bbq.port.out.RemoteImagePort;
import com.tastyhouse.application.mail.port.out.MailSender;
import com.tastyhouse.application.mail.service.MailVerificationService;
import com.tastyhouse.application.payment.port.out.PgPaymentGateway;
import com.tastyhouse.application.payment.port.out.PgProviderGateway;
import com.tastyhouse.application.payment.service.PgPaymentGatewayRouter;
import com.tastyhouse.application.region.port.out.AdminDongBoundaryPort;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shared.marker.SharedApp;
import com.tastyhouse.application.shared.marker.WebApp;
import com.tastyhouse.application.sms.port.out.SmsSender;
import com.tastyhouse.application.sms.service.SmsVerificationService;
import com.tastyhouse.architecture.AppOwnership;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

class AppIsolationTest {

    private static final List<Class<? extends Annotation>> MARKERS = AppOwnership.MARKERS;

    private static final Map<Class<? extends Annotation>, Set<String>> APP_RESTRICTED_PORTS = Map.of(
        WebApp.class, Set.of(
            MailSender.class.getName(),
            SmsSender.class.getName(),
            PgProviderGateway.class.getName(),
            SocialOAuthClient.class.getName()
        ),
        BatchApp.class, Set.of(
            BbqMenuPort.class.getName(),
            RemoteImagePort.class.getName(),
            AdminDongBoundaryPort.class.getName()
        )
    );

    private static final DescribedPredicate<JavaClass> SHARED_DOMAIN_SERVICE =
        new DescribedPredicate<>("..service..의 공유 도메인 서비스(*CommandService/*QueryService·UseCase 구현 제외)") {
            @Override
            public boolean test(JavaClass javaClass) {
                return isInServicePackage(javaClass)
                    && !javaClass.getSimpleName().endsWith("CommandService")
                    && !javaClass.getSimpleName().endsWith("QueryService")
                    && !implementsPortInInterface(javaClass);
            }
        };

    private static final DescribedPredicate<JavaClass> BEAN_CLASS =
        new DescribedPredicate<>("@Service/@Component(@Configuration 제외) 또는 마커만 단 도메인 서비스") {
            @Override
            public boolean test(JavaClass javaClass) {
                return isBeanClass(javaClass);
            }
        };

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    @Test
    void appsShouldNotDependOnEachOther() {
        for (Class<? extends Annotation> from : MARKERS) {
            for (Class<? extends Annotation> to : MARKERS) {
                if (from == to) {
                    continue;
                }
                DescribedPredicate<JavaClass> annotatedTarget =
                    DescribedPredicate.describe("@" + to.getSimpleName(), javaClass -> javaClass.isAnnotatedWith(to));
                DescribedPredicate<JavaClass> forbiddenTarget = from != SharedApp.class && to == SharedApp.class
                    ? annotatedTarget.and(not(SHARED_DOMAIN_SERVICE))
                        .as("@" + to.getSimpleName() + " (..service..의 공유 도메인 서비스 제외)")
                    : annotatedTarget;
                noClasses()
                    .that().areAnnotatedWith(from)
                    .should().dependOnClassesThat(forbiddenTarget)
                    .because(from.getSimpleName() + "는 " + to.getSimpleName() + "에 의존하지 않는다"
                        + " — 앱이 공유하는 것은 domain과 읽기 계약 + @SharedApp 리스너·공유 도메인 서비스뿐이고,"
                        + " 앱 → @SharedApp 도메인 서비스의 단방향 의존만 허용한다")
                    .check(classes);
            }
        }
    }

    @Test
    void constructorDependenciesShouldBeVisibleToApp() {
        List<JavaMethod> beanMethods = beanMethods();

        List<String> violations = new ArrayList<>();
        int checkedDependencies = 0;
        int checkedBeanMethodDependencies = 0;
        for (JavaCodeUnit injectionPoint : injectionPoints(beanMethods)) {
            Set<Class<? extends Annotation>> markers = AppOwnership.markersOf(injectionPoint.getOwner());
            if (markers.size() != 1) {
                continue;
            }
            Class<? extends Annotation> marker = markers.iterator().next();
            for (JavaClass dependency : parameterTypes(injectionPoint)) {
                if (!isProjectType(dependency)) {
                    continue;
                }
                checkedDependencies++;
                if (injectionPoint instanceof JavaMethod) {
                    checkedBeanMethodDependencies++;
                }
                if (!isVisibleTo(dependency, marker, beanMethods)) {
                    violations.add(injectionPoint.getFullName() + " (@" + marker.getSimpleName() + ") → "
                        + dependency.getName());
                }
            }
            for (JavaClass dependency : injectionPoint.getRawParameterTypes()) {
                if (dependency.isInterface() && isProjectType(dependency)
                    && visibleCandidateCount(dependency, marker, beanMethods) > 1) {
                    violations.add(injectionPoint.getFullName() + " (@" + marker.getSimpleName() + ") → "
                        + dependency.getName() + " (주입 후보가 2개 이상이라 모호하다)");
                }
            }
        }

        assertThat(checkedDependencies)
            .as("검사한 생성자·@Bean 파라미터 의존이 너무 적으면 타입 해석이 깨져 이 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(700);
        assertThat(checkedBeanMethodDependencies)
            .as("마커 @Configuration의 @Bean 파라미터 의존이 0개면 @Bean 검사가 공허하게 통과한다")
            .isGreaterThanOrEqualTo(3);
        assertThat(violations)
            .as("앱 마커 M이 붙은 클래스의 생성자 의존과 마커 M 설정의 @Bean 파라미터 의존은 앱 M의 컨텍스트에 떠야 한다 — "
                + "의존 타입이 M/@SharedApp 설정의 @Bean이거나, M/@SharedApp 마커를 갖거나, 인터페이스면 M/@SharedApp "
                + "구현체가 있거나 application 밖 구현이어야 한다. 컬렉션이 아닌 인터페이스 의존은 보이는 주입 후보가 "
                + "1개 이하여야 한다")
            .isEmpty();
    }

    @Test
    void appRestrictedPortDependentsShouldBelongToThatApp() {
        List<JavaMethod> beanMethods = beanMethods();

        List<String> dependents = new ArrayList<>();
        List<String> violations = new ArrayList<>();
        for (JavaCodeUnit injectionPoint : injectionPoints(beanMethods)) {
            Set<Class<? extends Annotation>> markers = AppOwnership.markersOf(injectionPoint.getOwner());
            if (markers.isEmpty()) {
                continue;
            }
            Set<String> injectedTypes = new HashSet<>();
            parameterTypes(injectionPoint).forEach(type -> injectedTypes.add(type.getName()));
            APP_RESTRICTED_PORTS.forEach((app, ports) -> {
                if (injectedTypes.stream().noneMatch(ports::contains)) {
                    return;
                }
                dependents.add(injectionPoint.getFullName());
                if (!markers.equals(Set.of(app))) {
                    violations.add(injectionPoint.getFullName() + "의 앱 마커 " + AppOwnership.describe(markers)
                        + " — @" + app.getSimpleName() + " 전용 포트를 주입받는다");
                }
            });
        }

        assertThat(dependents)
            .as("앱 전용 포트(web: 메일·SMS·PG·소셜 로그인 / batch: BBQ 메뉴·원격 이미지·행정동 경계)를 받는 주입 지점이 "
                + "줄면 타입 해석이 깨져 이 규칙이 공허하게 통과한다")
            .hasSizeGreaterThanOrEqualTo(9);
        assertThat(violations)
            .as("MailSender·SmsSender·PgProviderGateway·SocialOAuthClient 구현은 web에만, BbqMenuPort·RemoteImagePort·"
                + "AdminDongBoundaryPort 구현은 batch에만 실린다 — 이를 생성자나 마커 설정의 @Bean 파라미터로 받는 쪽은 "
                + "그 앱 마커 하나만 가져야 한다")
            .isEmpty();
    }

    @Test
    void sharedBeansShouldNotDependOnWebOnlyServices() {
        noClasses()
            .that().areAnnotatedWith(SharedApp.class)
            .should().dependOnClassesThat().belongToAnyOf(
                MailVerificationService.class,
                SmsVerificationService.class,
                PgPaymentGatewayRouter.class,
                PgPaymentGateway.class)
            .because("@WebApp 마커 서비스(web에만 뜨는 빈)를 공통 빈이 주입하면 admin·ceo·batch가 기동하지 못한다")
            .check(classes);
    }

    @Test
    void beansShouldHaveExactlyOneAppMarker() {
        classes()
            .that(BEAN_CLASS)
            .should(haveExactlyOneAppMarker())
            .because("마커 없는 @Service는 어느 앱에도 뜨지 않고(useDefaultFilters = false), 마커가 2개면 앱 격리가 깨진다")
            .check(classes);
    }

    @Test
    void useCasesShouldHaveExactlyOneAppMarker() {
        classes()
            .that().resideInAPackage("..port.in..").and().areInterfaces()
            .should(haveExactlyOneAppMarker())
            .because("Command record의 앱 소속은 이 마커에서 유도된다")
            .check(classes);
    }

    @Test
    void commandRecordsShouldBelongToExactlyOneApp() {
        Map<JavaClass, Set<Class<? extends Annotation>>> apps = AppOwnership.derive(classes);

        List<String> violations = new ArrayList<>();
        apps.forEach((record, markers) -> {
            if (markers.size() != 1) {
                violations.add(record.getName() + " → 소속 앱 " + markers.size() + "개 "
                    + AppOwnership.describe(markers)
                    + (markers.isEmpty() ? " (고아 — 어느 UseCase도 쓰지 않는다)" : " (앱 간 공유)"));
            }
        });

        assertThat(violations)
            .as("Command record는 정확히 한 앱에 속한다(유도 — AppOwnership 참조)")
            .isEmpty();
    }

    @Test
    void markerBeanCounts() {
        assertThat(countAnnotated(WebApp.class)).as("@WebApp 빈").isGreaterThanOrEqualTo(83);
        assertThat(countAnnotated(AdminApp.class)).as("@AdminApp 빈").isGreaterThanOrEqualTo(65);
        assertThat(countAnnotated(CeoApp.class)).as("@CeoApp 빈").isGreaterThanOrEqualTo(122);
        assertThat(countAnnotated(BatchApp.class)).as("@BatchApp 빈").isGreaterThanOrEqualTo(15);
        assertThat(countAnnotated(SharedApp.class)).as("@SharedApp 빈").isGreaterThanOrEqualTo(47);
    }

    @Test
    void markerUseCaseCounts() {
        assertThat(countUseCases(WebApp.class)).as("@WebApp UseCase").isGreaterThanOrEqualTo(50);
        assertThat(countUseCases(AdminApp.class)).as("@AdminApp UseCase").isGreaterThanOrEqualTo(100);
        assertThat(countUseCases(CeoApp.class)).as("@CeoApp UseCase").isGreaterThanOrEqualTo(95);
        assertThat(countUseCases(BatchApp.class)).as("@BatchApp UseCase").isEqualTo(7);
    }

    private long countAnnotated(Class<? extends Annotation> marker) {
        return classes.stream()
            .filter(AppIsolationTest::isBeanClass)
            .filter(c -> c.isAnnotatedWith(marker))
            .count();
    }

    private long countUseCases(Class<? extends Annotation> marker) {
        return classes.stream()
            .filter(JavaClass::isInterface)
            .filter(c -> c.getPackageName().contains(".port.in"))
            .filter(c -> c.isAnnotatedWith(marker))
            .count();
    }

    private static boolean isBeanClass(JavaClass javaClass) {
        boolean stereotyped = (javaClass.isAnnotatedWith(Service.class) || javaClass.isAnnotatedWith(Component.class))
            && !javaClass.isAnnotatedWith(Configuration.class);
        return stereotyped || isMarkerOnlyService(javaClass);
    }

    private static boolean isMarkerOnlyService(JavaClass javaClass) {
        return !javaClass.isInterface()
            && !javaClass.isAnnotation()
            && isInServicePackage(javaClass)
            && !javaClass.isAnnotatedWith(Service.class)
            && !javaClass.isAnnotatedWith(Component.class)
            && !javaClass.isAnnotatedWith(Configuration.class)
            && !AppOwnership.markersOf(javaClass).isEmpty();
    }

    private static boolean isInServicePackage(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return packageName.endsWith(".service") || packageName.contains(".service.");
    }

    private static boolean isProjectType(JavaClass javaClass) {
        String name = javaClass.getName();
        return name.startsWith("com.tastyhouse.application.") || name.startsWith("com.tastyhouse.domain.");
    }

    private boolean isVisibleTo(JavaClass dependency, Class<? extends Annotation> marker, List<JavaMethod> beanMethods) {
        boolean declaredAsBean = beanMethods.stream()
            .filter(method -> isVisibleMarker(method.getOwner(), marker))
            .anyMatch(method -> method.getRawReturnType().isAssignableTo(dependency.getName()));
        if (declaredAsBean) {
            return true;
        }
        if (!dependency.isInterface()) {
            return isVisibleMarker(dependency, marker);
        }
        List<JavaClass> implementations = implementationsOf(dependency);
        return implementations.isEmpty()
            || implementations.stream().anyMatch(implementation -> isVisibleMarker(implementation, marker));
    }

    private long visibleCandidateCount(
        JavaClass dependency,
        Class<? extends Annotation> marker,
        List<JavaMethod> beanMethods
    ) {
        long beans = beanMethods.stream()
            .filter(method -> isVisibleMarker(method.getOwner(), marker))
            .filter(method -> method.getRawReturnType().isAssignableTo(dependency.getName()))
            .count();
        long implementations = implementationsOf(dependency).stream()
            .filter(implementation -> isVisibleMarker(implementation, marker))
            .count();
        return beans + implementations;
    }

    private List<JavaClass> implementationsOf(JavaClass dependency) {
        return classes.stream()
            .filter(javaClass -> !javaClass.isInterface())
            .filter(javaClass -> !javaClass.getModifiers().contains(JavaModifier.ABSTRACT))
            .filter(javaClass -> javaClass.isAssignableTo(dependency.getName()))
            .filter(javaClass -> !isDecoratorOf(javaClass, dependency))
            .toList();
    }

    private static boolean isDecoratorOf(JavaClass implementation, JavaClass dependency) {
        return implementation.getConstructors().stream()
            .anyMatch(constructor -> constructor.getRawParameterTypes().contains(dependency));
    }

    private static boolean isVisibleMarker(JavaClass javaClass, Class<? extends Annotation> marker) {
        return javaClass.isAnnotatedWith(marker) || javaClass.isAnnotatedWith(SharedApp.class);
    }

    private List<JavaMethod> beanMethods() {
        return classes.stream()
            .filter(javaClass -> javaClass.isAnnotatedWith(Configuration.class))
            .flatMap(javaClass -> javaClass.getMethods().stream())
            .filter(method -> method.isAnnotatedWith(Bean.class))
            .toList();
    }

    private List<JavaCodeUnit> injectionPoints(List<JavaMethod> beanMethods) {
        List<JavaCodeUnit> injectionPoints = new ArrayList<>();
        classes.stream()
            .filter(javaClass -> !javaClass.isInterface() && !javaClass.isAnnotation())
            .forEach(javaClass -> injectionPoints.addAll(javaClass.getConstructors()));
        injectionPoints.addAll(beanMethods);
        return injectionPoints;
    }

    private static Set<JavaClass> parameterTypes(JavaCodeUnit codeUnit) {
        Set<JavaClass> types = new LinkedHashSet<>();
        Set<JavaType> visited = new HashSet<>();
        codeUnit.getParameterTypes().forEach(type -> collectTypes(type, types, visited));
        return types;
    }

    private static boolean implementsPortInInterface(JavaClass javaClass) {
        return javaClass.getAllRawInterfaces().stream().anyMatch(type -> {
            String packageName = type.getPackageName();
            return packageName.endsWith(".port.in") || packageName.contains(".port.in.");
        });
    }

    private static void collectTypes(JavaType type, Set<JavaClass> into, Set<JavaType> visited) {
        if (type == null || !visited.add(type)) {
            return;
        }
        into.add(type.toErasure());
        if (type instanceof JavaParameterizedType parameterized) {
            for (JavaType argument : parameterized.getActualTypeArguments()) {
                collectTypes(argument, into, visited);
            }
        }
    }

    private static ArchCondition<JavaClass> haveExactlyOneAppMarker() {
        return new ArchCondition<>("앱 마커를 정확히 1개 갖는다") {
            @Override
            public void check(JavaClass item, ConditionEvents events) {
                Set<Class<? extends Annotation>> found = AppOwnership.markersOf(item);
                boolean satisfied = found.size() == 1;
                events.add(new SimpleConditionEvent(item, satisfied,
                    item.getName() + "의 앱 마커 " + found.size() + "개 "
                        + AppOwnership.describe(found)));
            }
        };
    }
}
