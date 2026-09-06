package com.tastyhouse.domain.exception;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorCodeConventionTest {
    private static final Set<ErrorCode> NOT_FOUND_NAME_WITH_NON_404_STATUS = EnumSet.of(
        ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND,
        ErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND,
        ErrorCode.REFERRAL_REFERRER_NOT_FOUND,
        ErrorCode.FOLLOW_NOT_FOUND
    );

    private static final Set<ErrorCode> CODE_INTENTIONALLY_DIFFERS_FROM_NAME = EnumSet.of(
        ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND,
        ErrorCode.SMS_VERIFICATION_CODE_EXPIRED,
        ErrorCode.SMS_VERIFICATION_CODE_MISMATCH,
        ErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND,
        ErrorCode.MAIL_VERIFICATION_CODE_EXPIRED,
        ErrorCode.MAIL_VERIFICATION_CODE_MISMATCH
    );

    @Test
    @DisplayName("code 문자열은 전 상수에서 유일하다")
    void codesAreUnique() {
        Map<String, Long> duplicates = Arrays.stream(ErrorCode.values())
            .collect(Collectors.groupingBy(ErrorCode::getCode, Collectors.counting()))
            .entrySet().stream()
            .filter(entry -> entry.getValue() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThat(duplicates)
            .as("중복된 code 문자열이 있으면 클라이언트가 에러를 구분할 수 없다")
            .isEmpty();
    }

    @Test
    @DisplayName("신규 상수의 code 문자열은 상수명과 일치한다")
    void codeMatchesConstantName() {
        Map<String, String> mismatches = Arrays.stream(ErrorCode.values())
            .filter(errorCode -> !errorCode.name().equals(errorCode.getCode()))
            .filter(errorCode -> !CODE_INTENTIONALLY_DIFFERS_FROM_NAME.contains(errorCode))
            .collect(Collectors.toMap(Enum::name, ErrorCode::getCode));

        assertThat(mismatches)
            .as("상수명과 code가 다르면 로그의 상수명으로 응답 code를 검색할 수 없다")
            .isEmpty();
    }

    @Test
    @DisplayName("이름이 *_NOT_FOUND인 신규 상수는 404를 쓴다")
    void notFoundNamedCodesUse404() {
        Set<ErrorCode> violations = Arrays.stream(ErrorCode.values())
            .filter(errorCode -> errorCode.name().endsWith("_NOT_FOUND"))
            .filter(errorCode -> errorCode.getHttpStatusCode() != 404)
            .filter(errorCode -> !NOT_FOUND_NAME_WITH_NON_404_STATUS.contains(errorCode))
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(ErrorCode.class)));

        assertThat(violations)
            .as("*_NOT_FOUND 이름에는 404를 쓴다. 400이 의도라면 이름을 상황에 맞게 바꾼다(예: *_INVALID·*_EXPIRED)")
            .isEmpty();
    }

    @Test
    @DisplayName("봉인 목록의 상수는 실제로 규약 위반 상태로 남아 있다")
    void whitelistIsNotStale() {
        Set<ErrorCode> alreadyFixed = NOT_FOUND_NAME_WITH_NON_404_STATUS.stream()
            .filter(errorCode -> errorCode.getHttpStatusCode() == 404)
            .collect(Collectors.toCollection(() -> EnumSet.noneOf(ErrorCode.class)));

        assertThat(alreadyFixed)
            .as("규약을 지키도록 고쳐진 상수는 봉인 목록에서 제거한다")
            .isEmpty();
    }

    @Test
    @DisplayName("httpStatusCode는 유효한 HTTP 상태 범위 안에 있다")
    void httpStatusCodesAreInValidRange() {
        Map<String, Integer> invalid = Arrays.stream(ErrorCode.values())
            .filter(errorCode -> errorCode.getHttpStatusCode() < 400 || errorCode.getHttpStatusCode() > 599)
            .collect(Collectors.toMap(Enum::name, ErrorCode::getHttpStatusCode));

        assertThat(invalid)
            .as("에러코드는 4xx·5xx만 쓴다. 범위를 벗어나면 핸들러가 500으로 폴백해 의도한 상태가 사라진다")
            .isEmpty();
    }

    @Test
    @DisplayName("defaultMessage는 비어 있지 않다")
    void defaultMessagesAreNotBlank() {
        Set<String> blank = Arrays.stream(ErrorCode.values())
            .filter(errorCode -> errorCode.getDefaultMessage() == null || errorCode.getDefaultMessage().isBlank())
            .map(Enum::name)
            .collect(Collectors.toSet());

        assertThat(blank)
            .as("defaultMessage가 비면 message 없이 던진 예외의 응답 본문이 빈다")
            .isEmpty();
    }

    @Test
    @DisplayName("ErrorCode는 ErrorCodeSpec 계약을 만족한다")
    void implementsErrorCodeSpec() {
        Function<ErrorCodeSpec, String> codeReader = ErrorCodeSpec::getCode;

        assertThat(ErrorCode.ENTITY_NOT_FOUND).isInstanceOf(ErrorCodeSpec.class);
        assertThat(codeReader.apply(ErrorCode.ENTITY_NOT_FOUND)).isEqualTo("ENTITY_NOT_FOUND");
    }
}
