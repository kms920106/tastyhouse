package com.tastyhouse.application.architecture;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.application.payment.port.out.PgProviderCode;
import com.tastyhouse.domain.payment.model.PgProvider;

import static org.assertj.core.api.Assertions.assertThat;

class EnumCodeConstantsTest {

    @Test
    @DisplayName("PgProviderCode는 도메인 PgProvider와 상수명·순서가 같다 — 라우터가 name()으로 변환한다")
    void pgProviderCodeMatchesPgProvider() {
        assertThat(names(PgProviderCode.values())).isEqualTo(names(PgProvider.values()));
    }

    private static List<String> names(Enum<?>[] constants) {
        return Arrays.stream(constants).map(Enum::name).toList();
    }
}
