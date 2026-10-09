package com.tastyhouse.infrastructure.jpa.file.query;

import java.util.List;

import com.querydsl.core.types.Expression;
import com.querydsl.core.types.FactoryExpressionBase;
import com.querydsl.core.types.Visitor;

class FileUrlProjection extends FactoryExpressionBase<String> {

    private final Expression<String> filePath;
    private final transient FileUrlResolver resolver;

    FileUrlProjection(Expression<String> filePath, FileUrlResolver resolver) {
        super(String.class);
        this.filePath = filePath;
        this.resolver = resolver;
    }

    @Override
    public List<Expression<?>> getArgs() {
        return List.of(filePath);
    }

    @Override
    public String newInstance(Object... args) {
        return resolver.resolve((String) args[0]);
    }

    @Override
    public <R, C> R accept(Visitor<R, C> visitor, C context) {
        return visitor.visit(this, context);
    }
}
