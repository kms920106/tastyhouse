package com.tastyhouse.infrastructure.file.query;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.MappingProjection;

class FileUrlProjection extends MappingProjection<String> {
    private final Expression<String> filePath;
    private final transient FileUrlResolver resolver;

    FileUrlProjection(Expression<String> filePath, FileUrlResolver resolver) {
        super(String.class, filePath);
        this.filePath = filePath;
        this.resolver = resolver;
    }

    @Override
    protected String map(Tuple row) {
        return resolver.resolve(row.get(filePath));
    }
}
