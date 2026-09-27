package com.tastyhouse.logging;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SensitiveFieldMaskerTest {

    private final SensitiveFieldMasker masker = new SensitiveFieldMasker(new ObjectMapper());

    record LoginRequest(String username, String password) {
    }

    record TokenHolder(String name, LoginRequest login, List<LoginRequest> history) {
    }

    @Test
    @DisplayName("최상위 민감 필드를 *** 로 바꾸고 나머지 필드는 그대로 둔다")
    void masksTopLevelField() {
        String masked = masker.mask(new LoginRequest("tasty", "secret-pw"));

        assertThat(masked).isEqualTo("{\"username\":\"tasty\",\"password\":\"***\"}");
    }

    @Test
    @DisplayName("중첩 객체와 배열 원소 안의 민감 필드도 마스킹한다")
    void masksNestedObjectsAndArrayElements() {
        TokenHolder holder = new TokenHolder(
            "holder",
            new LoginRequest("a", "pw-a"),
            List.of(new LoginRequest("b", "pw-b"), new LoginRequest("c", "pw-c"))
        );

        String masked = masker.mask(holder);

        assertThat(masked).doesNotContain("pw-a", "pw-b", "pw-c");
        assertThat(masked).contains("\"name\":\"holder\"", "\"username\":\"b\"");
    }

    @Test
    @DisplayName("최상위가 배열이어도 원소마다 마스킹한다")
    void masksTopLevelArray() {
        String masked = masker.mask(List.of(Map.of("accessToken", "token-1")));

        assertThat(masked).isEqualTo("[{\"accessToken\":\"***\"}]");
    }

    @Test
    @DisplayName("null은 문자열 null로 기록한다")
    void masksNullAsLiteral() {
        assertThat(masker.mask(null)).isEqualTo("null");
    }
}
