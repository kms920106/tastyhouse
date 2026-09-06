package com.tastyhouse.application.architecture;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaType;

import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.application.shared.marker.BatchApp;
import com.tastyhouse.application.shared.marker.CeoApp;
import com.tastyhouse.application.shared.marker.WebApp;

public final class AppOwnership {

    private static final Map<String, Class<? extends Annotation>> DESERIALIZED_COMMANDS = Map.of(
        "com.tastyhouse.application.shop.port.in.ShopStorePriceVerificationItemCommand", CeoApp.class);

    public static final List<Class<? extends Annotation>> MARKERS =
        List.of(WebApp.class, AdminApp.class, CeoApp.class, BatchApp.class);

    private AppOwnership() {
    }

    public static Map<JavaClass, Set<Class<? extends Annotation>>> derive(JavaClasses classes) {
        List<JavaClass> commandRecords = classes.stream()
            .filter(AppOwnership::isCommandRecord)
            .toList();

        Map<JavaClass, Set<Class<? extends Annotation>>> apps = new HashMap<>();
        for (JavaClass record : commandRecords) {
            Set<Class<? extends Annotation>> seed = new LinkedHashSet<>();
            Class<? extends Annotation> declared = DESERIALIZED_COMMANDS.get(record.getName());
            if (declared != null) {
                seed.add(declared);
            }
            apps.put(record, seed);
        }

        classes.stream()
            .filter(JavaClass::isInterface)
            .filter(c -> c.getPackageName().contains(".port.in"))
            .forEach(useCase -> {
                Set<Class<? extends Annotation>> useCaseMarkers = markersOf(useCase);
                if (useCaseMarkers.isEmpty()) {
                    return;
                }
                for (JavaClass referenced : signatureTypes(useCase)) {
                    Set<Class<? extends Annotation>> target = apps.get(referenced);
                    if (target != null) {
                        target.addAll(useCaseMarkers);
                    }
                }
            });

        boolean changed = true;
        while (changed) {
            changed = false;
            for (JavaClass parent : commandRecords) {
                Set<Class<? extends Annotation>> parentApps = apps.get(parent);
                if (parentApps.isEmpty()) {
                    continue;
                }
                for (JavaClass component : componentTypes(parent)) {
                    Set<Class<? extends Annotation>> childApps = apps.get(component);
                    if (childApps != null && childApps.addAll(parentApps)) {
                        changed = true;
                    }
                }
            }
        }

        return apps;
    }

    public static Set<Class<? extends Annotation>> markersOf(JavaClass javaClass) {
        Set<Class<? extends Annotation>> found = new LinkedHashSet<>();
        for (Class<? extends Annotation> marker : MARKERS) {
            if (javaClass.isAnnotatedWith(marker)) {
                found.add(marker);
            }
        }
        return found;
    }

    public static boolean isCommandRecord(JavaClass javaClass) {
        return javaClass.isRecord() && javaClass.getPackageName().contains(".port.in");
    }

    private static Set<JavaClass> signatureTypes(JavaClass useCase) {
        Set<JavaClass> types = new LinkedHashSet<>();
        for (JavaMethod method : useCase.getMethods()) {
            collect(method.getReturnType(), types);
            method.getParameterTypes().forEach(type -> collect(type, types));
        }
        return types;
    }

    private static Set<JavaClass> componentTypes(JavaClass record) {
        Set<JavaClass> types = new LinkedHashSet<>();
        record.getFields().forEach(field -> {
            collect(field.getType(), types);
            collect(field.getRawType(), types);
        });
        return types;
    }

    private static void collect(JavaType type, Set<JavaClass> into) {
        collect(type, into, new HashSet<>());
    }

    private static void collect(JavaType type, Set<JavaClass> into, Set<JavaType> visited) {
        if (type == null || !visited.add(type)) {
            return;
        }
        into.add(type.toErasure());
        if (type instanceof com.tngtech.archunit.core.domain.JavaParameterizedType parameterized) {
            for (JavaType argument : parameterized.getActualTypeArguments()) {
                collect(argument, into, visited);
            }
        }
    }

    public static String describe(Set<Class<? extends Annotation>> markers) {
        if (markers.isEmpty()) {
            return "(없음)";
        }
        List<String> names = new ArrayList<>();
        markers.forEach(marker -> names.add(marker.getSimpleName()));
        return String.join(", ", names);
    }
}
