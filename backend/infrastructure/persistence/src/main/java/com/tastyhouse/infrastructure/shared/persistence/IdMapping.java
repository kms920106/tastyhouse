package com.tastyhouse.infrastructure.shared.persistence;

import java.util.function.Function;

public final class IdMapping {
    private IdMapping() {
    }

    public static <T> T vo(Long raw, Function<Long, T> factory) {
        return raw == null ? null : factory.apply(raw);
    }

    public static <T> Long raw(T vo, Function<T, Long> extractor) {
        return vo == null ? null : extractor.apply(vo);
    }
}
