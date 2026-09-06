package com.tastyhouse.domain.architecture;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.dependencies.SliceRule;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static org.assertj.core.api.Assertions.assertThat;

class ContextBoundaryTest {
    private static final String DOMAIN_ROOT = "com.tastyhouse.domain";

    private static final Set<String> NON_CONTEXT_PACKAGES = Set.of("shared", "exception");

    private static final Set<String> ALLOWED_CROSS_CONTEXT_SUBPACKAGES = Set.of("vo", "event", "port");

    private static final Set<String> FORBIDDEN_CROSS_CONTEXT_SUBPACKAGES = Set.of("model", "repository", "service");

    private static final Set<String> SEALED_VIOLATIONS = Set.of(
        "com.tastyhouse.domain.mail.service.MailVerificationService",
        "com.tastyhouse.domain.member.service.MemberDeliveryAddressService",
        "com.tastyhouse.domain.order.service.OrderPlacementService",
        "com.tastyhouse.domain.payment.service.PaymentCancellationService",
        "com.tastyhouse.domain.payment.service.PaymentConfirmationService",
        "com.tastyhouse.domain.reservation.service.ReservationBookingService",
        "com.tastyhouse.domain.review.service.ReviewBlindRequestService",
        "com.tastyhouse.domain.review.service.ReviewLifecycleService",
        "com.tastyhouse.domain.review.service.ReviewOwnerReplyService",
        "com.tastyhouse.domain.shop.service.DeliveryAreaProjection",
        "com.tastyhouse.domain.shop.service.ShopCeoAssignmentService",
        "com.tastyhouse.domain.shop.service.ShopDeliveryAreaPolygonService",
        "com.tastyhouse.domain.shop.service.ShopDeliveryAreaRadiusService",
        "com.tastyhouse.domain.shop.service.ShopDeliveryAreaService",
        "com.tastyhouse.domain.shop.service.ShopDeliveryTipService",
        "com.tastyhouse.domain.shop.service.ShopRequestCancelService"
    );

    private static final Set<String> SEALED_CYCLES = Set.of(
        "order,product,review,shop"
    );

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(DOMAIN_ROOT);

    @Test
    void contextsShouldNotDependOnInternalsOfOtherContexts() {
        ArchRule rule = classes()
            .that(new DescribedPredicate<>("컨텍스트에 속한다") {
                @Override
                public boolean test(JavaClass javaClass) {
                    return contextOf(javaClass.getName()) != null;
                }
            })
            .should(new com.tngtech.archunit.lang.ArchCondition<>(
                "타 컨텍스트의 model/repository/service를 import하지 않아야 한다(봉인 목록 제외)") {
                @Override
                public void check(JavaClass javaClass, ConditionEvents events) {
                    if (SEALED_VIOLATIONS.contains(topLevelNameOf(javaClass))) {
                        return;
                    }
                    for (String violation : crossContextViolationsOf(javaClass)) {
                        events.add(SimpleConditionEvent.violated(javaClass,
                            javaClass.getName() + "가 타 컨텍스트 내부를 import한다: " + violation));
                    }
                }
            })
            .because("타 컨텍스트는 ID VO(vo)·도메인 이벤트(event)·출력 포트(port)로만 참조한다");

        rule.check(classes);
    }

    @Test
    void sealedViolationsShouldNotBeStale() {
        Set<String> actualViolators = new TreeSet<>();
        for (JavaClass javaClass : classes) {
            if (contextOf(javaClass.getName()) == null) {
                continue;
            }
            if (!crossContextViolationsOf(javaClass).isEmpty()) {
                actualViolators.add(topLevelNameOf(javaClass));
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
    void contextsShouldBeFreeOfCycles() {
        SliceRule rule = SlicesRuleDefinition.slices()
            .matching(DOMAIN_ROOT + ".(*)..")
            .namingSlices("$1")
            .should().beFreeOfCycles();

        for (String component : SEALED_CYCLES) {
            Set<String> contexts = Set.of(component.split(","));
            rule = rule.ignoreDependency(inAnyContext(contexts), inAnyContext(contexts));
        }

        rule.because("컨텍스트 간 순환은 경계를 무의미하게 만든다(기존 성분 2개는 봉인)")
            .check(classes);
    }

    @Test
    void sealedCyclesShouldNotBeStale() {
        Set<String> stale = new TreeSet<>(SEALED_CYCLES);
        stale.removeAll(actualCycleComponents());

        assertThat(stale)
            .as("봉인 목록과 일치하지 않는 순환 성분 — 실제 성분에 맞춰 SEALED_CYCLES를 갱신할 것"
                + " (현재 실제 성분: " + actualCycleComponents() + ")")
            .isEmpty();
    }

    private static DescribedPredicate<JavaClass> inAnyContext(Set<String> contexts) {
        return new DescribedPredicate<>(contexts + " 성분에 속한다") {
            @Override
            public boolean test(JavaClass javaClass) {
                String context = contextOf(javaClass.getName());
                return context != null && contexts.contains(context);
            }
        };
    }

    private Set<String> actualCycleComponents() {
        Map<String, Set<String>> edges = new TreeMap<>();
        for (JavaClass javaClass : classes) {
            String from = contextOf(javaClass.getName());
            if (from == null) {
                continue;
            }
            for (JavaClass target : javaClass.getDirectDependenciesFromSelf().stream()
                .map(Dependency::getTargetClass).toList()) {
                String to = contextOf(target.getName());
                if (to != null && !to.equals(from)) {
                    edges.computeIfAbsent(from, key -> new TreeSet<>()).add(to);
                }
            }
        }
        return stronglyConnectedComponents(edges);
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

    private static Set<String> crossContextViolationsOf(JavaClass javaClass) {
        String from = contextOf(javaClass.getName());
        Set<String> violations = new TreeSet<>();
        for (JavaClass target : javaClass.getDirectDependenciesFromSelf().stream()
            .map(Dependency::getTargetClass).toList()) {
            String to = contextOf(target.getName());
            if (to == null || to.equals(from)) {
                continue;
            }
            String subpackage = subpackageOf(target.getName(), to);
            if (subpackage != null && FORBIDDEN_CROSS_CONTEXT_SUBPACKAGES.contains(subpackage)) {
                violations.add(to + "." + subpackage);
            }
        }
        return violations;
    }

    private static String contextOf(String className) {
        if (!className.startsWith(DOMAIN_ROOT + ".")) {
            return null;
        }
        String remainder = className.substring(DOMAIN_ROOT.length() + 1);
        int dot = remainder.indexOf('.');
        if (dot < 0) {
            return null;
        }
        String context = remainder.substring(0, dot);
        return NON_CONTEXT_PACKAGES.contains(context) ? null : context;
    }

    private static String subpackageOf(String className, String context) {
        String remainder = className.substring(DOMAIN_ROOT.length() + context.length() + 2);

        int lastDot = remainder.lastIndexOf('.');
        if (lastDot < 0) {
            return null;
        }
        for (String segment : remainder.substring(0, lastDot).split("\\.")) {
            if (FORBIDDEN_CROSS_CONTEXT_SUBPACKAGES.contains(segment)
                || ALLOWED_CROSS_CONTEXT_SUBPACKAGES.contains(segment)) {
                return segment;
            }
        }
        return null;
    }

    private static String topLevelNameOf(JavaClass javaClass) {
        String name = javaClass.getName();
        int dollar = name.indexOf('$');
        return dollar < 0 ? name : name.substring(0, dollar);
    }
}
