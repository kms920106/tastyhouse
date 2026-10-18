package com.tastyhouse.application.architecture;

import java.util.Set;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static org.assertj.core.api.Assertions.assertThat;

class ExecutorCallerTransactionRulesTest {

    private static final String NEW_TX_SUFFIX = "InNewTx";

    private static final Set<String> TRANSACTIONAL_ANNOTATIONS = Set.of(
        "org.springframework.transaction.annotation.Transactional",
        "jakarta.transaction.Transactional"
    );

    private final JavaClasses classes = new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages("com.tastyhouse.application");

    @Test
    void newTxCallersExistSoTheRuleIsNotVacuous() {
        long callerClasses = classes.stream()
            .filter(javaClass -> javaClass.getMethodCallsFromSelf().stream()
                .anyMatch(call -> call.getTarget().getName().endsWith(NEW_TX_SUFFIX)))
            .count();

        assertThat(callerClasses).isGreaterThanOrEqualTo(4);
    }

    @Test
    void newTxCallersShouldNotBeTransactional() {
        ArchRule rule = methods()
            .that(callAnInNewTxMethod())
            .should(notRunInsideTransaction())
            .because("Executor의 *InNewTx 메서드는 Propagation.REQUIRED라서 호출자가 트랜잭션 안이면 새 트랜잭션이 열리지 않고, "
                + "호출자가 하는 PG HTTP 호출이 DB 트랜잭션 안으로 들어간다. 이 가드는 직접 호출자만 본다");

        rule.check(classes);
    }

    private static DescribedPredicate<JavaMethod> callAnInNewTxMethod() {
        return new DescribedPredicate<>("call a method named *" + NEW_TX_SUFFIX) {
            @Override
            public boolean test(JavaMethod method) {
                return method.getMethodCallsFromSelf().stream()
                    .anyMatch(call -> call.getTarget().getName().endsWith(NEW_TX_SUFFIX));
            }
        };
    }

    private static ArchCondition<JavaMethod> notRunInsideTransaction() {
        return new ArchCondition<>("not be @Transactional nor be declared in a @Transactional class") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                JavaClass owner = method.getOwner();
                boolean methodTransactional = TRANSACTIONAL_ANNOTATIONS.stream().anyMatch(method::isAnnotatedWith);
                boolean ownerTransactional = TRANSACTIONAL_ANNOTATIONS.stream().anyMatch(owner::isAnnotatedWith);
                if (methodTransactional || ownerTransactional) {
                    events.add(SimpleConditionEvent.violated(
                        method,
                        method.getFullName() + "가 *" + NEW_TX_SUFFIX + "를 호출하지만 트랜잭션 안에서 실행된다"
                    ));
                }
            }
        };
    }
}
