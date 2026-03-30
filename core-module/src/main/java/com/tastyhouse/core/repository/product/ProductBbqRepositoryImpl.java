package com.tastyhouse.core.repository.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.product.ProductBbq;
import com.tastyhouse.core.entity.product.QProductBbq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ProductBbqRepositoryImpl implements ProductBbqRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ProductBbq> findFirstByIsOptionsSyncedFalse() {
        QProductBbq productBbq = QProductBbq.productBbq;

        return Optional.ofNullable(queryFactory
            .selectFrom(productBbq)
            .where(productBbq.isOptionsSynced.isFalse())
            .fetchFirst());
    }
}
