package com.tastyhouse.infrastructure.shared.query;

import java.util.function.Function;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.MappingProjection;

public class EnumLabelProjection<E extends Enum<E>> extends MappingProjection<String> {
    private final Expression<E> source;
    private final transient Function<E, String> label;

    private EnumLabelProjection(Expression<E> source, Function<E, String> label) {
        super(String.class, source);
        this.source = source;
        this.label = label;
    }

    public static <E extends Enum<E>> Expression<String> labelOf(Expression<E> source, Function<E, String> label) {
        return new EnumLabelProjection<>(source, label);
    }

    @Override
    protected String map(Tuple row) {
        E value = row.get(source);
        return value != null ? label.apply(value) : null;
    }
}
