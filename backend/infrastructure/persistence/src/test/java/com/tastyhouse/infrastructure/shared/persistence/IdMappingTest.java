package com.tastyhouse.infrastructure.shared.persistence;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;

import static org.assertj.core.api.Assertions.assertThat;

class IdMappingTest {
    @Test
    @DisplayName("vo: raw가 null이면 factory를 호출하지 않고 null을 반환한다")
    void voReturnsNullWithoutInvokingFactoryWhenRawIsNull() {
        AtomicBoolean invoked = new AtomicBoolean(false);
        Long absentRaw = nullRaw();

        CeoId result = IdMapping.vo(absentRaw, raw -> {
            invoked.set(true);
            return CeoId.of(raw);
        });

        assertThat(result).isNull();
        assertThat(invoked).isFalse();
    }

    @Test
    @DisplayName("vo: raw가 있으면 factory에 위임해 VO로 승격한다")
    void voDelegatesToFactoryWhenRawIsPresent() {
        CeoId result = IdMapping.vo(1L, CeoId::of);

        assertThat(result).isEqualTo(CeoId.of(1L));
    }

    @Test
    @DisplayName("raw: vo가 null이면 extractor를 호출하지 않고 null을 반환한다")
    void rawReturnsNullWithoutInvokingExtractorWhenVoIsNull() {
        AtomicBoolean invoked = new AtomicBoolean(false);
        CeoId absentVo = nullVo();

        Long result = IdMapping.raw(absentVo, (CeoId vo) -> {
            invoked.set(true);
            return vo.value();
        });

        assertThat(result).isNull();
        assertThat(invoked).isFalse();
    }

    private static Long nullRaw() {
        return null;
    }

    private static CeoId nullVo() {
        return null;
    }

    @Test
    @DisplayName("raw: vo가 있으면 extractor로 언패킹한다")
    void rawDelegatesToExtractorWhenVoIsPresent() {
        Long result = IdMapping.raw(CeoId.of(1L), CeoId::value);

        assertThat(result).isEqualTo(1L);
    }
}
