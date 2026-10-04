package com.tastyhouse.application.architecture;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceContextBoundaryTest {

    private static final String DOMAIN_ROOT = "com.tastyhouse.domain";

    private static final String APPLICATION_ROOT = "com.tastyhouse.application";

    private static final Set<String> NON_CONTEXT_PACKAGES = Set.of("shared", "exception");

    private static final String CONFIGURATION = "org.springframework.context.annotation.Configuration";

    private static final Set<String> EXCLUDED_COLLABORATORS = Set.of(
        "com.tastyhouse.application.auth.service.AuthPasswordResetService",
        "com.tastyhouse.application.auth.service.CredentialLoginService",
        "com.tastyhouse.application.auth.service.PhoneLoginService",
        "com.tastyhouse.application.auth.service.apple.AppleSocialLoginService",
        "com.tastyhouse.application.auth.service.facebook.FacebookSocialLoginService",
        "com.tastyhouse.application.auth.service.kakao.KakaoSocialLoginService",
        "com.tastyhouse.application.auth.service.naver.NaverSocialLoginService",
        "com.tastyhouse.application.member.service.MemberAuthService",
        "com.tastyhouse.application.member.service.MemberGradeService",
        "com.tastyhouse.application.member.service.MemberReviewService",
        "com.tastyhouse.application.member.service.MemberShopService",
        "com.tastyhouse.application.payment.service.PaymentCancellationExecutor",
        "com.tastyhouse.application.payment.service.PaymentConfirmationExecutor",
        "com.tastyhouse.application.product.service.ProductImageSpecValidator",
        "com.tastyhouse.application.product.service.ProductNameValidator",
        "com.tastyhouse.application.product.service.ProductOptionGroupOwnershipValidator",
        "com.tastyhouse.application.productsoldout.service.ProductSoldOutReleaseExecutor",
        "com.tastyhouse.application.region.service.AdminDongSyncExecutor",
        "com.tastyhouse.application.region.service.AdminDongSyncRunner",
        "com.tastyhouse.application.reservation.service.ReservationBookingExecutor",
        "com.tastyhouse.application.reviewblind.service.ReviewBlindExpirationExecutor",
        "com.tastyhouse.application.shop.service.OwnedShopIdProvider",
        "com.tastyhouse.application.shop.service.ShopFoodTypeCategoryReader",
        "com.tastyhouse.application.shop.service.ShopImageSpecValidator",
        "com.tastyhouse.application.shop.service.ShopMenuCollectionImageSpecValidator",
        "com.tastyhouse.application.shop.service.ShopOwnershipValidator",
        "com.tastyhouse.application.shop.service.StorePriceListImageSpecValidator",
        "com.tastyhouse.application.shop.service.StorePriceVerificationOwnerReader",
        "com.tastyhouse.application.shop.service.StorePriceVerificationReader"
    );

    private static final Set<String> SEALED_VIOLATIONS = Set.of(
        "com.tastyhouse.application.mail.service.MailVerificationService",
        "com.tastyhouse.application.member.service.MemberDeliveryAddressService",
        "com.tastyhouse.application.order.service.OrderPlacementService",
        "com.tastyhouse.application.payment.service.PaymentCancellationService",
        "com.tastyhouse.application.payment.service.PaymentConfirmationService",
        "com.tastyhouse.application.reservation.service.ReservationBookingService",
        "com.tastyhouse.application.review.service.ReviewBlindRequestService",
        "com.tastyhouse.application.review.service.ReviewLifecycleService",
        "com.tastyhouse.application.review.service.ReviewOwnerReplyService",
        "com.tastyhouse.application.shop.service.ShopCeoAssignmentService",
        "com.tastyhouse.application.shop.service.ShopDeliveryAreaPolygonService",
        "com.tastyhouse.application.shop.service.ShopDeliveryAreaRadiusService",
        "com.tastyhouse.application.shop.service.ShopDeliveryAreaService",
        "com.tastyhouse.application.shop.service.ShopDeliveryTipService",
        "com.tastyhouse.application.shop.service.ShopRequestCancelService"
    );

    private static final Set<String> SEALED_CYCLES = Set.of(
        "order,product,review,shop"
    );

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(APPLICATION_ROOT, DOMAIN_ROOT);

    @Test
    void domainServicesShouldNotDependOnInternalsOfOtherContexts() {
        List<String> violations = new ArrayList<>();
        for (JavaClass javaClass : domainServiceClasses()) {
            String owner = topLevelOf(javaClass).getName();
            if (SEALED_VIOLATIONS.contains(owner)) {
                continue;
            }
            for (String violation : crossContextViolationsOf(javaClass)) {
                violations.add(javaClass.getName() + " → " + violation);
            }
        }

        assertThat(violations)
            .as("도메인 서비스(application ..service..에서 UseCase를 구현하지 않는 도메인 서비스)는 타 컨텍스트를 ID VO·이벤트·"
                + "포트(port.out, write 제외)로만 참조한다 — 타 컨텍스트의 model·write 포트·service 직접 참조 금지(봉인 목록 제외)")
            .isEmpty();
    }

    @Test
    void sealedViolationsShouldNotBeStale() {
        Set<String> actualViolators = new TreeSet<>();
        for (JavaClass javaClass : domainServiceClasses()) {
            if (!crossContextViolationsOf(javaClass).isEmpty()) {
                actualViolators.add(topLevelOf(javaClass).getName());
            }
        }

        Set<String> stale = new TreeSet<>(SEALED_VIOLATIONS);
        stale.removeAll(actualViolators);

        assertThat(stale)
            .as("봉인 목록에 있으나 더 이상 위반하지 않는 클래스 — SEALED_VIOLATIONS에서 지울 것")
            .isEmpty();
    }

    @Test
    void sealedViolationListShouldNotBeEmpty() {
        assertThat(SEALED_VIOLATIONS)
            .as("봉인 대상이 0건이면 봉인 장치(SEALED_VIOLATIONS·짝 테스트)를 제거하고 규칙을 순수 강제로 전환할 것")
            .isNotEmpty();
    }

    @Test
    void domainServicesShouldExist() {
        assertThat(domainServices().size())
            .as("대상 도메인 서비스가 0건이면 경계 규칙이 공허하게 통과한다")
            .isGreaterThanOrEqualTo(71);
    }

    @Test
    void domainServiceContextsShouldBeFreeOfCycles() {
        Set<String> unsealed = new TreeSet<>(actualCycleComponents());
        unsealed.removeAll(SEALED_CYCLES);

        assertThat(unsealed)
            .as("도메인 서비스가 만드는 컨텍스트 간 순환 성분 — 봉인 목록 밖의 성분은 금지")
            .isEmpty();
    }

    @Test
    void sealedCyclesShouldNotBeStale() {
        Set<String> actual = actualCycleComponents();
        Set<String> stale = new TreeSet<>(SEALED_CYCLES);
        stale.removeAll(actual);

        assertThat(stale)
            .as("봉인 목록과 일치하지 않는 순환 성분 — 실제 성분에 맞춰 SEALED_CYCLES를 갱신할 것"
                + " (현재 실제 성분: " + actual + ")")
            .isEmpty();
    }

    @Test
    void excludedCollaboratorsShouldNotBeStale() {
        Set<String> structural = new TreeSet<>();
        for (JavaClass javaClass : classes) {
            if (javaClass.getName().startsWith(APPLICATION_ROOT + ".")
                && contextOf(javaClass.getName()) != null
                && subpackageSegments(javaClass.getName()).contains("service")
                && javaClass.isTopLevelClass()
                && isStructuralDomainService(javaClass)) {
                structural.add(javaClass.getName());
            }
        }

        Set<String> stale = new TreeSet<>(EXCLUDED_COLLABORATORS);
        stale.removeAll(structural);

        assertThat(stale)
            .as("제외 목록의 클래스가 사라졌거나 이미 구조 조건으로 빠진다 — 목록에서 지운다(목록은 줄어들기만 한다)")
            .isEmpty();
    }

    private static boolean isStructuralDomainService(JavaClass javaClass) {
        return !javaClass.isInterface()
            && !javaClass.getSimpleName().endsWith("CommandService")
            && !javaClass.getSimpleName().endsWith("QueryService")
            && !javaClass.isAnnotatedWith(CONFIGURATION)
            && javaClass.getAllRawInterfaces().stream().noneMatch(ServiceContextBoundaryTest::isPortInInterface);
    }

    private static boolean isPortInInterface(JavaClass javaClass) {
        String packageName = javaClass.getPackageName();
        return javaClass.isInterface() && (packageName.endsWith(".port.in") || packageName.contains(".port.in."));
    }

    private List<JavaClass> domainServices() {
        return classes.stream()
            .filter(javaClass -> javaClass.getName().startsWith(APPLICATION_ROOT + "."))
            .filter(javaClass -> contextOf(javaClass.getName()) != null)
            .filter(javaClass -> subpackageSegments(javaClass.getName()).contains("service"))
            .filter(JavaClass::isTopLevelClass)
            .filter(ServiceContextBoundaryTest::isStructuralDomainService)
            .filter(javaClass -> !EXCLUDED_COLLABORATORS.contains(javaClass.getName()))
            .toList();
    }

    private List<JavaClass> domainServiceClasses() {
        Set<JavaClass> services = Set.copyOf(domainServices());
        return classes.stream()
            .filter(javaClass -> services.contains(topLevelOf(javaClass)))
            .toList();
    }

    private Set<String> actualCycleComponents() {
        Map<String, Set<String>> edges = new TreeMap<>();
        for (JavaClass javaClass : domainServiceClasses()) {
            String from = contextOf(javaClass.getName());
            for (JavaClass target : targetsOf(javaClass)) {
                String to = contextOf(target.getName());
                if (to != null && !to.equals(from)) {
                    edges.computeIfAbsent(from, key -> new TreeSet<>()).add(to);
                }
            }
        }
        return stronglyConnectedComponents(edges);
    }

    private static List<JavaClass> targetsOf(JavaClass javaClass) {
        return javaClass.getDirectDependenciesFromSelf().stream()
            .map(Dependency::getTargetClass)
            .map(ServiceContextBoundaryTest::topLevelOf)
            .distinct()
            .toList();
    }

    private static JavaClass topLevelOf(JavaClass javaClass) {
        JavaClass current = javaClass;
        while (current.getEnclosingClass().isPresent()) {
            current = current.getEnclosingClass().get();
        }
        return current;
    }

    private static Set<String> crossContextViolationsOf(JavaClass javaClass) {
        String from = contextOf(javaClass.getName());
        Set<String> violations = new TreeSet<>();
        for (JavaClass target : targetsOf(javaClass)) {
            String to = contextOf(target.getName());
            if (to == null || to.equals(from)) {
                continue;
            }
            String forbidden = forbiddenSubpackageOf(target.getName());
            if (forbidden != null) {
                violations.add(to + "." + forbidden + " (" + target.getSimpleName() + ")");
            }
        }
        return violations;
    }

    private static String forbiddenSubpackageOf(String className) {
        List<String> segments = subpackageSegments(className);
        if (className.startsWith(APPLICATION_ROOT + ".")) {
            if (segments.contains("write")) {
                return "port.out.write";
            }
            boolean outboundPort = segments.size() >= 2
                && segments.get(0).equals("port") && segments.get(1).equals("out");
            return outboundPort ? null : String.join(".", segments);
        }
        for (String segment : segments) {
            if (segment.equals("model")) {
                return segment;
            }
            if (segment.equals("vo") || segment.equals("event")) {
                return null;
            }
        }
        return null;
    }

    private static String contextOf(String className) {
        String root = rootOf(className);
        if (root == null) {
            return null;
        }
        String remainder = className.substring(root.length() + 1);
        int dot = remainder.indexOf('.');
        if (dot < 0) {
            return null;
        }
        String context = remainder.substring(0, dot);
        return NON_CONTEXT_PACKAGES.contains(context) ? null : context;
    }

    private static List<String> subpackageSegments(String className) {
        String root = rootOf(className);
        String context = contextOf(className);
        if (root == null || context == null) {
            return List.of();
        }
        String remainder = className.substring(root.length() + context.length() + 2);
        int lastDot = remainder.lastIndexOf('.');
        if (lastDot < 0) {
            return List.of();
        }
        return Arrays.asList(remainder.substring(0, lastDot).split("\\."));
    }

    private static String rootOf(String className) {
        if (className.startsWith(DOMAIN_ROOT + ".")) {
            return DOMAIN_ROOT;
        }
        if (className.startsWith(APPLICATION_ROOT + ".")) {
            return APPLICATION_ROOT;
        }
        return null;
    }

    private static Set<String> stronglyConnectedComponents(Map<String, Set<String>> edges) {
        Map<String, Integer> index = new HashMap<>();
        Map<String, Integer> lowLink = new HashMap<>();
        Deque<String> componentStack = new ArrayDeque<>();
        Set<String> onStack = new HashSet<>();
        Set<String> components = new TreeSet<>();
        int[] counter = {0};

        for (String root : edges.keySet()) {
            if (index.containsKey(root)) {
                continue;
            }

            Deque<Object[]> frames = new ArrayDeque<>();
            frames.push(new Object[] {root, 0});
            index.put(root, counter[0]);
            lowLink.put(root, counter[0]++);
            componentStack.push(root);
            onStack.add(root);

            while (!frames.isEmpty()) {
                Object[] frame = frames.peek();
                String node = (String) frame[0];
                List<String> neighbours = List.copyOf(edges.getOrDefault(node, Set.of()));
                int next = (int) frame[1];

                if (next < neighbours.size()) {
                    frame[1] = next + 1;
                    String neighbour = neighbours.get(next);
                    if (!index.containsKey(neighbour)) {
                        index.put(neighbour, counter[0]);
                        lowLink.put(neighbour, counter[0]++);
                        componentStack.push(neighbour);
                        onStack.add(neighbour);
                        frames.push(new Object[] {neighbour, 0});
                    } else if (onStack.contains(neighbour)) {
                        lowLink.put(node, Math.min(lowLink.get(node), index.get(neighbour)));
                    }
                    continue;
                }

                frames.pop();
                if (!frames.isEmpty()) {
                    String parent = (String) frames.peek()[0];
                    lowLink.put(parent, Math.min(lowLink.get(parent), lowLink.get(node)));
                }
                if (lowLink.get(node).equals(index.get(node))) {
                    Set<String> component = new TreeSet<>();
                    String popped;
                    do {
                        popped = componentStack.pop();
                        onStack.remove(popped);
                        component.add(popped);
                    } while (!popped.equals(node));
                    if (component.size() > 1) {
                        components.add(String.join(",", component));
                    }
                }
            }
        }
        return components;
    }
}
