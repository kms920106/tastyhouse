package com.tastyhouse.application.shared.exception;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.DomainErrorCode;
import com.tastyhouse.domain.exception.ErrorCodeSpec;
import com.tastyhouse.apicommon.exception.ApiErrorCode;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCatalogConventionTest {

    private static final Set<String> MIRRORED_CODES = Set.of("AUTH_REQUIRED");

    private static final Set<String> NOT_FOUND_NAME_WITH_NON_404_STATUS = Set.of(
        "SMS_VERIFICATION_CODE_NOT_FOUND",
        "MAIL_VERIFICATION_CODE_NOT_FOUND",
        "REFERRAL_REFERRER_NOT_FOUND",
        "FOLLOW_NOT_FOUND"
    );

    private static final Set<String> CODE_INTENTIONALLY_DIFFERS_FROM_NAME = Set.of(
        "SMS_VERIFICATION_CODE_NOT_FOUND",
        "SMS_VERIFICATION_CODE_EXPIRED",
        "SMS_VERIFICATION_CODE_MISMATCH",
        "MAIL_VERIFICATION_CODE_NOT_FOUND",
        "MAIL_VERIFICATION_CODE_EXPIRED",
        "MAIL_VERIFICATION_CODE_MISMATCH"
    );

    private static final List<ErrorCatalog.Entry> ENTRIES = ErrorCatalog.all();

    @Test
    @DisplayName("code 문자열은 모든 카탈로그를 통틀어 유일하다(봉인된 미러 제외)")
    void codesAreUniqueAcrossCatalogs() {
        Map<String, List<String>> duplicates = ENTRIES.stream()
            .collect(Collectors.groupingBy(
                ErrorCatalog.Entry::code,
                Collectors.mapping(entry -> entry.catalog() + "." + entry.name(), Collectors.toList())
            ))
            .entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .filter(entry -> !MIRRORED_CODES.contains(entry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(duplicates)
            .as("같은 code를 두 카탈로그에 두지 않는다. 여러 모듈이 던지면 더 안쪽 모듈(application 코어·domain)로 옮긴다")
            .isEmpty();
    }

    @Test
    @DisplayName("봉인된 미러 code는 실제로 두 카탈로그에 있고 status·message가 같다")
    void mirroredCodesStayIdentical() {
        assertThat(ApiErrorCode.AUTH_REQUIRED.getHttpStatusCode()).isEqualTo(WebErrorCode.AUTH_REQUIRED.getHttpStatusCode());
        assertThat(ApiErrorCode.AUTH_REQUIRED.getCode()).isEqualTo(WebErrorCode.AUTH_REQUIRED.getCode());
        assertThat(ApiErrorCode.AUTH_REQUIRED.getDefaultMessage()).isEqualTo(WebErrorCode.AUTH_REQUIRED.getDefaultMessage());

        Map<String, Long> counts = ENTRIES.stream()
            .filter(entry -> MIRRORED_CODES.contains(entry.code()))
            .collect(Collectors.groupingBy(ErrorCatalog.Entry::code, Collectors.counting()));
        assertThat(counts.keySet()).isEqualTo(MIRRORED_CODES);
        assertThat(counts)
            .as("미러가 해소됐으면 봉인 목록에서 제거한다")
            .allSatisfy((code, count) -> assertThat(count).isEqualTo(2L));
    }

    @Test
    @DisplayName("code는 상수명과 같다(봉인 목록 제외)")
    void codeMatchesConstantName() {
        Map<String, String> mismatches = ENTRIES.stream()
            .filter(entry -> !CODE_INTENTIONALLY_DIFFERS_FROM_NAME.contains(entry.name()))
            .filter(entry -> !entry.name().equals(entry.code()))
            .collect(Collectors.toMap(entry -> entry.catalog() + "." + entry.name(), ErrorCatalog.Entry::code));

        assertThat(mismatches).isEmpty();
    }

    @Test
    @DisplayName("*_NOT_FOUND 이름은 404를 쓴다(봉인 목록 제외)")
    void notFoundNamedCodesUse404() {
        Set<String> violations = ENTRIES.stream()
            .filter(entry -> entry.name().endsWith("_NOT_FOUND"))
            .filter(entry -> !NOT_FOUND_NAME_WITH_NON_404_STATUS.contains(entry.name()))
            .filter(entry -> entry.status() != 404)
            .map(entry -> entry.catalog() + "." + entry.name())
            .collect(Collectors.toSet());

        assertThat(violations)
            .as("*_NOT_FOUND 이름에는 404를 쓴다. 400이 의도라면 이름을 상황에 맞게 바꾼다(예: *_INVALID·*_EXPIRED)")
            .isEmpty();
    }

    @Test
    @DisplayName("봉인 목록의 상수는 모두 존재하고 실제로 규약 위반 상태로 남아 있다")
    void sealedListsAreNotStale() {
        Map<String, ErrorCatalog.Entry> byName = ENTRIES.stream()
            .collect(Collectors.toMap(ErrorCatalog.Entry::name, Function.identity(), (left, right) -> left));

        assertThat(NOT_FOUND_NAME_WITH_NON_404_STATUS)
            .as("규약을 지키도록 고쳐졌거나 사라진 상수는 봉인 목록에서 제거한다")
            .allSatisfy(name -> assertThat(byName.get(name)).isNotNull().extracting(ErrorCatalog.Entry::status).isNotEqualTo(404));
        assertThat(CODE_INTENTIONALLY_DIFFERS_FROM_NAME)
            .allSatisfy(name -> assertThat(byName.get(name)).isNotNull().extracting(ErrorCatalog.Entry::code).isNotEqualTo(name));
    }

    @Test
    @DisplayName("httpStatusCode는 4xx·5xx 범위 안에 있다")
    void httpStatusCodesAreInValidRange() {
        Set<String> invalid = ENTRIES.stream()
            .filter(entry -> entry.status() < 400 || entry.status() > 599)
            .map(entry -> entry.catalog() + "." + entry.name())
            .collect(Collectors.toSet());

        assertThat(invalid)
            .as("범위를 벗어나면 핸들러가 500으로 폴백해 의도한 상태가 사라진다")
            .isEmpty();
    }

    @Test
    @DisplayName("defaultMessage는 비어 있지 않다")
    void defaultMessagesAreNotBlank() {
        Set<String> blank = ENTRIES.stream()
            .filter(entry -> entry.message() == null || entry.message().isBlank())
            .map(entry -> entry.catalog() + "." + entry.name())
            .collect(Collectors.toSet());

        assertThat(blank).isEmpty();
    }

    @Test
    @DisplayName("domain·application 카탈로그는 ErrorCodeSpec을, application 카탈로그는 ApplicationErrorCodeSpec을 구현한다")
    void catalogsImplementTheirLayerContract() {
        assertThat(ErrorCodeSpec.class).isAssignableFrom(DomainErrorCode.class);
        assertThat(List.<Class<?>>of(DomainErrorCode.class)).noneMatch(ApplicationErrorCodeSpec.class::isAssignableFrom);
        List.of(ApplicationErrorCode.class, WebErrorCode.class, AdminErrorCode.class, CeoErrorCode.class, BatchErrorCode.class)
            .forEach(type -> assertThat(ApplicationErrorCodeSpec.class).isAssignableFrom(type));
    }
}
