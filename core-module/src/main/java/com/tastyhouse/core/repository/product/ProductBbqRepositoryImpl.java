package com.tastyhouse.core.repository.product;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.entity.product.ProductBbq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.tastyhouse.core.entity.product.QProductBbq.productBbq;

@Repository
@RequiredArgsConstructor
public class ProductBbqRepositoryImpl implements ProductBbqRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<ProductBbq> findFirstByIsOptionsSyncedFalse() {
        return Optional.ofNullable(queryFactory
            .selectFrom(productBbq)
            .where(productBbq.isOptionsSynced.isFalse())
            .fetchFirst());
    }
}
