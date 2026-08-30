package com.tastyhouse.infrastructure.shared.persistence;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link IdMapping}의 null-안전 계약을 검증한다. Spring/JPA 컨텍스트 없이 순수 정적 유틸만 검증하는
 * 단위 테스트다.
 */
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

    /**
     * null 인자를 리터럴이 아니라 변수로 넘기기 위한 헬퍼. 리터럴 {@code null}을 그대로 전달하면 정적 분석기가
     * {@code IdMapping.vo}의 null 분기를 접어 "결과가 항상 null"이라고 경고하는데, 그 경고는 이 테스트가
     * 검증하려는 계약(null 입력 시 factory 미호출) 자체를 가리키므로 의미가 없다. 런타임 동작은 동일하다.
     */
    private static Long nullRaw() {
        return null;
    }

    /**
     * {@link #nullRaw()}의 VO 버전 — {@code IdMapping.raw}에 null VO를 변수로 전달하기 위한 헬퍼.
     */
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
