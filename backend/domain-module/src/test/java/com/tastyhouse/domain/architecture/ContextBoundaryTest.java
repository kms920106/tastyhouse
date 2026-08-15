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

/**
 * 도메인 컨텍스트 경계 규칙(ArchUnit).
 *
 * <p>domain-module에는 25개 바운디드 컨텍스트가 한 모듈에 공존한다. 컨텍스트 간 참조는
 * <strong>ID VO({@code <ctx>.vo..})·도메인 이벤트({@code <ctx>.event..})·출력 포트
 * ({@code <ctx>.port..})</strong> 세 가지로만 허용하고, 타 컨텍스트의
 * {@code model..}/{@code repository..}/{@code service..} 직접 import는 금지한다.
 * {@code shared..}·{@code exception..}은 컨텍스트가 아니라 전 컨텍스트 공용이므로 전면 허용한다.
 *
 * <p><strong>기존 위반은 고치지 않고 봉인한다.</strong> 이 단계의 목표는 전면 재설계가 아니라
 * "현상 동결 + 신규 위반 차단"이며, 실제 결합 해소는 후속 단계가 담당한다. 봉인 방식은
 * {@code ErrorCodeConventionTest}의 봉인 목록 선례를 그대로 따른 수동 {@code Set}이고,
 * 목록이 낡으면(위반이 실제로 고쳐졌으면) {@link #sealedViolationsShouldNotBeStale()}가
 * 실패해 알려준다 — 봉인이 영구 면죄부가 되지 않게 하는 짝 테스트다.
 *
 * <p><strong>봉인 목록에 새 항목을 추가하지 말 것.</strong> 신규 코드는 규칙을 지킨다.
 *
 * <p>모든 규칙은 {@code allowEmptyShould(true)} 없이 선언한다(공허 통과 금지 —
 * {@code DomainPurityTest}·{@code LayerRulesTest} 개정 선례).
 */
class ContextBoundaryTest {

    private static final String DOMAIN_ROOT = "com.tastyhouse.domain";

    /**
     * 컨텍스트가 아니라 전 컨텍스트 공용인 최상위 패키지. 이 둘로의 의존은 경계 위반이 아니다.
     */
    private static final Set<String> NON_CONTEXT_PACKAGES = Set.of("shared", "exception");

    /**
     * 타 컨텍스트에서 import해도 되는 하위 패키지 — ID VO(애그리거트 간 FK 표현)·도메인 이벤트·출력 포트.
     */
    private static final Set<String> ALLOWED_CROSS_CONTEXT_SUBPACKAGES = Set.of("vo", "event", "port");

    /**
     * 타 컨텍스트에서 import하면 안 되는 하위 패키지 — 애그리거트 내부 구현.
     */
    private static final Set<String> FORBIDDEN_CROSS_CONTEXT_SUBPACKAGES = Set.of("model", "repository", "service");

    /**
     * 규칙 도입 시점에 이미 존재하던 위반 클래스 목록(현상 동결).
     *
     * <p>이 목록은 <strong>줄어들기만 해야 한다.</strong> 항목을 추가하는 것은 새 위반을 승인하는 것이므로
     * 금지한다. 위반을 해소했다면 이 목록에서 그 클래스를 지운다 —
     * {@link #sealedViolationsShouldNotBeStale()}가 지우지 않은 것을 잡아준다.
     */
    private static final Set<String> SEALED_VIOLATIONS = Set.of(
        "com.tastyhouse.domain.mail.service.MailVerificationService",
        "com.tastyhouse.domain.member.referral.service.ReferralRegistrationService",
        "com.tastyhouse.domain.member.service.MemberDeliveryAddressService",
        "com.tastyhouse.domain.order.model.Order",
        "com.tastyhouse.domain.order.service.OrderPlacement",
        "com.tastyhouse.domain.order.service.OrderPlacementService",
        "com.tastyhouse.domain.order.vo.OrderDeliveryDestination",
        "com.tastyhouse.domain.order.vo.OrderSchedule",
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

    /**
     * 규칙 도입 시점에 이미 존재하던 컨텍스트 간 순환 성분(현상 동결).
     *
     * <p><strong>쌍이 아니라 강결합 성분(SCC) 단위로 봉인한다.</strong> {@code SliceRule#beFreeOfCycles}는
     * 2노드 상호 참조뿐 아니라 {@code order → product → shop → order} 같은 <em>전이 순환</em>까지
     * 잡으므로, 봉인 목록도 같은 단위여야 한다. 쌍으로 적으면 두 모델이 어긋나
     * "쌍 하나를 지우라"는 짝 테스트의 지시를 따랐을 때 정작 전이 순환이 드러나 규칙이 깨진다
     * (실제로 {@code product}는 아래 4-노드 성분에 속하는데 쌍 표기로는 이름이 등장하지 않았다).
     *
     * <p>항목 형태는 성분에 속한 컨텍스트를 알파벳 오름차순으로 이은 {@code "a,b,c"}다.
     */
    private static final Set<String> SEALED_CYCLES = Set.of(
        "member,point",
        "order,product,review,shop"
    );

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(DOMAIN_ROOT);

    /**
     * 컨텍스트는 타 컨텍스트의 {@code model}/{@code repository}/{@code service}를 직접 import하지 않는다.
     * 애그리거트 간 참조는 ID VO·도메인 이벤트·출력 포트로만 표현한다. 봉인 목록에 등재된 기존 위반은 통과시킨다.
     */
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

    /**
     * 봉인 목록이 낡지 않았음을 보장하는 짝 테스트. 목록에 있는데 실제로는 더 이상 위반하지 않는 클래스가
     * 있으면(= 결합이 해소됐으면) 실패해서 목록에서 지우라고 알려준다. 봉인이 영구 면죄부가 되지 않게 하는 장치다.
     */
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

    /**
     * 봉인 목록 자체가 비어 있지 않은지 확인한다. 목록이 비면 위 짝 테스트가 검사할 대상이 없어져
     * 공허하게 통과하므로, 봉인이 전부 해소된 시점에는 이 테스트가 실패해
     * <strong>봉인 장치 자체를 제거하라</strong>고 알려준다.
     */
    @Test
    void sealedViolationListShouldNotBeEmpty() {
        assertThat(SEALED_VIOLATIONS)
            .as("봉인 대상이 0건이면 봉인 장치(SEALED_VIOLATIONS·짝 테스트)를 제거하고 규칙을 순수 강제로 전환할 것")
            .isNotEmpty();
    }

    /**
     * 컨텍스트 간 순환 의존을 금지한다. 기존 순환 성분 2개는 봉인 목록으로 제외하고 신규 순환만 실패시킨다.
     */
    @Test
    void contextsShouldBeFreeOfCycles() {
        SliceRule rule = SlicesRuleDefinition.slices()
            .matching(DOMAIN_ROOT + ".(*)..")
            .namingSlices("$1")
            .should().beFreeOfCycles();

        // 봉인 성분 "안쪽" 의존만 제외한다 — 양 끝이 모두 같은 성분에 속할 때만 무시하므로,
        // 봉인 컨텍스트가 관여하더라도 성분 밖으로 나가는 의존(예: order→member)은 그대로 검사된다.
        // 성분에 걸린 컨텍스트를 통째로 무시하면 무관한 신규 순환까지 함께 가려진다.
        for (String component : SEALED_CYCLES) {
            Set<String> contexts = Set.of(component.split(","));
            rule = rule.ignoreDependency(inAnyContext(contexts), inAnyContext(contexts));
        }

        rule.because("컨텍스트 간 순환은 경계를 무의미하게 만든다(기존 성분 2개는 봉인)")
            .check(classes);
    }

    /**
     * 순환 봉인 목록이 낡지 않았음을 보장하는 짝 테스트. 봉인된 성분이 실제로는 더 이상 순환하지 않거나
     * 더 작아졌으면 실패해서 목록을 갱신하라고 알려준다.
     *
     * <p>비교 단위가 {@code SliceRule}과 동일한 <strong>강결합 성분(SCC)</strong>이어야 한다 —
     * 2노드 쌍으로 비교하면 전이 순환을 놓쳐, 봉인을 지우라고 지시해 놓고 정작 규칙은 깨지는
     * 모순이 생긴다.
     */
    @Test
    void sealedCyclesShouldNotBeStale() {
        Set<String> stale = new TreeSet<>(SEALED_CYCLES);
        stale.removeAll(actualCycleComponents());

        assertThat(stale)
            .as("봉인 목록과 일치하지 않는 순환 성분 — 실제 성분에 맞춰 SEALED_CYCLES를 갱신할 것"
                + " (현재 실제 성분: " + actualCycleComponents() + ")")
            .isEmpty();
    }

    /**
     * 주어진 컨텍스트 집합 중 하나에 속하는지 판정하는 술어. {@code ignoreDependency(from, to)}의
     * 양쪽에 <em>같은 성분</em>을 걸어, 그 성분 내부의 의존만 순환 검사에서 제외한다.
     */
    private static DescribedPredicate<JavaClass> inAnyContext(Set<String> contexts) {
        return new DescribedPredicate<>(contexts + " 성분에 속한다") {
            @Override
            public boolean test(JavaClass javaClass) {
                String context = contextOf(javaClass.getName());
                return context != null && contexts.contains(context);
            }
        };
    }

    /**
     * 현재 코드베이스에 실재하는 컨텍스트 간 순환 성분(크기 2 이상인 강결합 성분)을 계산한다.
     * 형태는 성분 소속 컨텍스트를 알파벳 오름차순으로 이은 {@code "a,b,c"}다.
     *
     * <p>{@code SliceRule#beFreeOfCycles}와 <strong>같은 순환 개념</strong>이어야 하므로 SCC로 구한다 —
     * 2노드 상호 참조만 세면 {@code order → product → shop → order} 같은 전이 순환을 놓친다.
     * 의존 범위도 규칙과 동일하게 {@code vo}/{@code event}/{@code port}를 포함한 전부를 본다.
     */
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

    /**
     * Tarjan 알고리즘으로 강결합 성분을 구해, 크기 2 이상인 것만 {@code "a,b,c"} 형태로 돌려준다.
     * 재귀 대신 명시적 스택을 써서 컨텍스트 수가 늘어도 스택 깊이에 영향받지 않게 한다.
     */
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
            // (노드, 다음에 볼 이웃 인덱스) 프레임을 직접 관리하는 반복형 Tarjan.
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

    /**
     * 한 클래스가 저지르는 컨텍스트 경계 위반을 {@code "<ctx>.<subpackage>"} 형태로 모아 돌려준다.
     * 위반이 없으면 빈 집합이다.
     *
     * <p>ArchUnit의 의존 그래프(import뿐 아니라 필드·시그니처·본문 참조까지 포함)를 근거로 삼는다 —
     * 같은 패키지의 타입을 import 없이 참조하는 경우는 컨텍스트가 다르면 애초에 발생하지 않으므로
     * import 스캔과 결과는 같고, 어노테이션·제네릭 파라미터 같은 간접 참조까지 잡힌다.
     */
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

    /**
     * 클래스 FQCN에서 소속 컨텍스트명을 뽑는다. domain-module 밖이거나
     * {@code shared}/{@code exception}(비컨텍스트 공용)이면 {@code null}이다.
     */
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

    /**
     * 컨텍스트 안에서의 하위 패키지명(예: {@code model}/{@code vo}/{@code service})을 뽑는다.
     *
     * <p>{@code member.follow}·{@code member.referral}처럼 컨텍스트 아래 한 겹이 더 있는 경우에는
     * 그 다음 세그먼트를 본다 — {@code member.referral.service.X}의 하위 패키지는 {@code service}다.
     * 판정은 "허용/금지 목록에 있는 세그먼트를 앞에서부터 찾는" 방식이라 중첩 깊이에 무관하다.
     */
    private static String subpackageOf(String className, String context) {
        String remainder = className.substring(DOMAIN_ROOT.length() + context.length() + 2);
        // 마지막 세그먼트는 클래스명이므로 스캔 대상에서 뺀다. 포함하면 컨텍스트 루트에 놓인
        // 클래스명이 우연히 세그먼트 목록과 겹칠 때(예: <ctx>.Event, <ctx>.Model) 하위 패키지로
        // 오인된다 — event는 실제 컨텍스트명이자 허용 세그먼트라 이 충돌이 한 걸음 거리에 있다.
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

    /**
     * 중첩 클래스·익명 클래스의 이름을 최상위 클래스 이름으로 정규화한다. 봉인 목록은 파일 단위(최상위 클래스)로
     * 관리하므로, {@code Xxx$1}·{@code Xxx$Inner}가 별도 항목으로 등재될 필요가 없게 한다.
     */
    private static String topLevelNameOf(JavaClass javaClass) {
        String name = javaClass.getName();
        int dollar = name.indexOf('$');
        return dollar < 0 ? name : name.substring(0, dollar);
    }
}
