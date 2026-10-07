package com.tastyhouse.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;

public final class UseCaseServices {

    private UseCaseServices() {
    }

    public static boolean isUseCaseService(JavaClass javaClass) {
        return !javaClass.isInterface()
            && inPackageSegment(javaClass.getPackageName(), "service")
            && javaClass.getAllRawInterfaces().stream().anyMatch(UseCaseServices::isPortIn)
            && !ModuleOrigin.isFrom(javaClass, ModuleOrigin.BATCH);
    }

    public static boolean isQueryService(JavaClass javaClass) {
        return isUseCaseService(javaClass)
            && javaClass.getAllRawInterfaces().stream()
                .filter(UseCaseServices::isPortIn)
                .anyMatch(port -> port.getSimpleName().endsWith("QueryUseCase"));
    }

    public static boolean isCommandService(JavaClass javaClass) {
        return isUseCaseService(javaClass)
            && !isQueryService(javaClass);
    }

    public static DescribedPredicate<JavaClass> commands() {
        return new DescribedPredicate<>("명령 유스케이스 서비스(Query가 아닌 port.in 구현)") {
            @Override
            public boolean test(JavaClass javaClass) {
                return isCommandService(javaClass);
            }
        };
    }

    public static DescribedPredicate<JavaClass> queries() {
        return new DescribedPredicate<>("조회 유스케이스 서비스(…QueryUseCase 구현)") {
            @Override
            public boolean test(JavaClass javaClass) {
                return isQueryService(javaClass);
            }
        };
    }

    private static boolean inPackageSegment(String packageName, String segment) {
        return packageName.endsWith("." + segment) || packageName.contains("." + segment + ".");
    }

    private static boolean isPortIn(JavaClass javaInterface) {
        return inPackageSegment(javaInterface.getPackageName(), "port.in");
    }
}
