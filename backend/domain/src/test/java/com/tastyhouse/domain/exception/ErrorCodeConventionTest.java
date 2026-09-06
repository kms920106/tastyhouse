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

/**
 * {@link ErrorCode} 카탈로그의 규약을 강제하는 가드 테스트.
 *
 * <p>이 enum은 228개가 넘는 상수를 담은 단일 카탈로그이고 상태코드가 상수별 수작업으로 지정되므로,
 * 신규 상수를 추가할 때 이름과 HTTP 상태가 어긋나는 것을 사람 눈으로 잡기 어렵다. 이 테스트가 그 규약을 대신 지킨다.
 */
class ErrorCodeConventionTest {

    /**
     * 이름은 {@code *_NOT_FOUND}인데 404가 아닌 기존 상수들.
     *
     * <p>이 값들은 이미 프론트엔드가 분기하는 wire 계약(응답 status + code)이므로 지금 고치면 클라이언트가 깨진다.
     * 따라서 교정 대상이 아니라 <b>봉인 대상</b>이다. 여기에 새 항목을 추가하지 말고, 신규 상수는 규약을 지킨다.
     */
    private static final Set<ErrorCode> NOT_FOUND_NAME_WITH_NON_404_STATUS = EnumSet.of(
        ErrorCode.SMS_VERIFICATION_CODE_NOT_FOUND,
        ErrorCode.MAIL_VERIFICATION_CODE_NOT_FOUND,
        ErrorCode.REFERRAL_REFERRER_NOT_FOUND,
        ErrorCode.FOLLOW_NOT_FOUND
    );

    /**
     * 상수명과 응답 {@code code}가 의도적으로 다른 상수들.
     *
     * <p>채널 도메인 어휘 통일(mail/sms)로 상수명은 {@code SMS_}·{@code MAIL_} 접두어로 대칭화했지만,
     * 응답 {@code code} 문자열은 프론트가 분기하는 wire 계약이라 예전 값({@code VERIFICATION_CODE_*}·
     * {@code EMAIL_VERIFICATION_CODE_*})을 유지했다. 루트 {@code CLAUDE.md}의
     * "채널 도메인 어휘 통일 규칙"에 명시된 의도적 불일치이므로 교정 대상이 아니다.
     */
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
