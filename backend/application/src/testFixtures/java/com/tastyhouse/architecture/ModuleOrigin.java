package com.tastyhouse.architecture;

import java.net.URI;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.Source;

public final class ModuleOrigin {

    public static final String CORE = "application";
    public static final String WEB = "web-application";
    public static final String ADMIN = "admin-application";
    public static final String CEO = "ceo-application";
    public static final String BATCH = "batch-application";
    public static final List<String> APP_MODULES = List.of(WEB, ADMIN, CEO, BATCH);

    private static final Pattern JAR_FILE_NAME = Pattern.compile("^(.+?)-\\d[^/]*\\.jar$");

    private ModuleOrigin() {
    }

    public static String of(JavaClass javaClass) {
        URI uri = javaClass.getSource()
            .map(Source::getUri)
            .orElseThrow(() -> new IllegalStateException("클래스 출처를 알 수 없다: " + javaClass.getName()));
        return of(uri);
    }

    public static boolean isFrom(JavaClass javaClass, String module) {
        return of(javaClass).equals(module);
    }

    public static DescribedPredicate<JavaClass> from(String module) {
        return new DescribedPredicate<>(module + " 출처") {
            @Override
            public boolean test(JavaClass javaClass) {
                return isFrom(javaClass, module);
            }
        };
    }

    public static String of(URI uri) {
        String location = uri.toString();
        if (location.startsWith("jar:")) {
            int entrySeparator = location.indexOf("!/");
            if (entrySeparator < 0 || location.indexOf("!/", entrySeparator + 2) >= 0) {
                throw new IllegalStateException("jar 위치에서 모듈을 판정할 수 없다: " + location);
            }
            String jarPath = location.substring("jar:".length(), entrySeparator);
            String fileName = jarPath.substring(jarPath.lastIndexOf('/') + 1);
            Matcher matcher = JAR_FILE_NAME.matcher(fileName);
            if (!matcher.matches() || fileName.endsWith("-test-fixtures.jar")) {
                throw new IllegalStateException("jar 파일명에서 모듈을 판정할 수 없다: " + location);
            }
            return matcher.group(1);
        }
        if (uri.getPath() == null) {
            throw new IllegalStateException("클래스 위치에서 모듈을 판정할 수 없다: " + location);
        }
        String[] segments = uri.getPath().split("/");
        for (int i = segments.length - 4; i > 0; i--) {
            if (segments[i].equals("build") && segments[i + 1].equals("classes") && segments[i + 3].equals("main")) {
                return segments[i - 1];
            }
        }
        throw new IllegalStateException("클래스 디렉터리에서 모듈을 판정할 수 없다: " + location);
    }
}
