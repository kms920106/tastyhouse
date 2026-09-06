package com.tastyhouse.infrastructure.shop.query;

import com.tastyhouse.application.shop.port.out.ShopNoticeOwnerQueryPort;
import com.tastyhouse.application.shop.port.out.ShopNoticeManagementQueryPort;
import com.tastyhouse.application.shop.port.out.ShopNoticeQueryPort;
import com.tastyhouse.application.shop.port.out.ShopNoticeManagementListItemResult;
import com.tastyhouse.application.shop.port.out.ShopNoticeResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopJpaEntity.shopJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopNoticeImageJpaEntity.shopNoticeImageJpaEntity;
import static com.tastyhouse.infrastructure.shop.persistence.QShopNoticeJpaEntity.shopNoticeJpaEntity;

@Repository
public class ShopNoticeQueryDao implements ShopNoticeQueryPort, ShopNoticeOwnerQueryPort, ShopNoticeManagementQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopNoticeQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public List<ShopNoticeResult> findNotices(Long shopId) {
        List<ShopNoticeRow> rows = queryFactory
            .select(Projections.constructor(ShopNoticeRow.class,
                shopNoticeJpaEntity.id,
                shopNoticeJpaEntity.shopId,
                shopNoticeJpaEntity.content,
                shopNoticeJpaEntity.exposed,
                shopNoticeJpaEntity.hidden,
                shopNoticeJpaEntity.createdAt,
                shopNoticeJpaEntity.updatedAt
            ))
            .from(shopNoticeJpaEntity)
            .where(shopNoticeJpaEntity.shopId.eq(shopId))
            .orderBy(shopNoticeJpaEntity.exposed.desc(), shopNoticeJpaEntity.createdAt.desc())
            .fetch();

        Map<Long, List<String>> imageUrls = findImageUrlsByNoticeIds(rows.stream().map(ShopNoticeRow::id).toList());

        return rows.stream()
            .map(row -> new ShopNoticeResult(
                row.id(),
                row.shopId(),
                row.content(),
                imageUrls.getOrDefault(row.id(), List.of()),
                row.exposed(),
                row.hidden(),
                row.createdAt(),
                row.updatedAt()
            ))
            .toList();
    }

    @Override
    public Optional<ShopNoticeResult> findExposedNotice(Long shopId) {
        return Optional.ofNullable(queryFactory
                .select(Projections.constructor(ShopNoticeRow.class,
                    shopNoticeJpaEntity.id,
                    shopNoticeJpaEntity.shopId,
                    shopNoticeJpaEntity.content,
                    shopNoticeJpaEntity.exposed,
                    shopNoticeJpaEntity.hidden,
                    shopNoticeJpaEntity.createdAt,
                    shopNoticeJpaEntity.updatedAt
                ))
                .from(shopNoticeJpaEntity)
                .where(
                    shopNoticeJpaEntity.shopId.eq(shopId),
                    shopNoticeJpaEntity.exposed.isTrue(),
                    shopNoticeJpaEntity.hidden.isFalse()
                )
                .fetchFirst())
            .map(row -> new ShopNoticeResult(
                row.id(),
                row.shopId(),
                row.content(),
                findImageUrlsByNoticeIds(List.of(row.id())).getOrDefault(row.id(), List.of()),
                row.exposed(),
                row.hidden(),
                row.createdAt(),
                row.updatedAt()
            ));
    }

    @Override
    public PageResult<ShopNoticeManagementListItemResult> findNoticePage(
        Long shopId,
        String shopName,
        Boolean hidden,
        PageQuery pageQuery
    ) {
        Long total = queryFactory
            .select(shopNoticeJpaEntity.count())
            .from(shopNoticeJpaEntity)
            .join(shopJpaEntity).on(shopJpaEntity.id.eq(shopNoticeJpaEntity.shopId))
            .where(
                shopIdEq(shopId),
                shopNameContains(shopName),
                hiddenEq(hidden)
            )
            .fetchOne();

        if (total == null || total == 0) {
            return PageResult.empty(pageQuery.page(), pageQuery.size());
        }

        List<ShopNoticeManagementRow> rows = queryFactory
            .select(Projections.constructor(ShopNoticeManagementRow.class,
                shopNoticeJpaEntity.id,
                shopNoticeJpaEntity.shopId,
                shopJpaEntity.name,
                shopNoticeJpaEntity.content,
                shopNoticeJpaEntity.exposed,
                shopNoticeJpaEntity.hidden,
                shopNoticeJpaEntity.createdAt
            ))
            .from(shopNoticeJpaEntity)
            .join(shopJpaEntity).on(shopJpaEntity.id.eq(shopNoticeJpaEntity.shopId))
            .where(
                shopIdEq(shopId),
                shopNameContains(shopName),
                hiddenEq(hidden)
            )
            .orderBy(shopNoticeJpaEntity.createdAt.desc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        Map<Long, List<String>> imageUrls =
            findImageUrlsByNoticeIds(rows.stream().map(ShopNoticeManagementRow::id).toList());

        List<ShopNoticeManagementListItemResult> content = rows.stream()
            .map(row -> new ShopNoticeManagementListItemResult(
                row.id(),
                row.shopId(),
                row.shopName(),
                row.content(),
                imageUrls.getOrDefault(row.id(), List.of()),
                row.exposed(),
                row.hidden(),
                row.createdAt()
            ))
            .toList();

        return PageResult.of(content, total, pageQuery.page(), pageQuery.size());
    }

    private Map<Long, List<String>> findImageUrlsByNoticeIds(List<Long> noticeIds) {
        if (noticeIds.isEmpty()) {
            return Map.of();
        }

        return queryFactory
            .select(Projections.constructor(ShopNoticeImageResult.class,
                shopNoticeImageJpaEntity.shopNoticeId,
                uploadedFileJpaEntity.filePath,
                shopNoticeImageJpaEntity.sortOrder
            ))
            .from(shopNoticeImageJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(shopNoticeImageJpaEntity.imageFileId))
            .where(shopNoticeImageJpaEntity.shopNoticeId.in(noticeIds))
            .orderBy(shopNoticeImageJpaEntity.shopNoticeId.asc(), shopNoticeImageJpaEntity.sortOrder.asc())
            .fetch()
            .stream()
            .map(row -> new ShopNoticeImageResult(
                row.shopNoticeId(),
                fileUrlResolver.resolve(row.imageUrl()),
                row.sortOrder()
            ))
            .filter(row -> row.imageUrl() != null)
            .collect(Collectors.groupingBy(
                ShopNoticeImageResult::shopNoticeId,
                Collectors.mapping(ShopNoticeImageResult::imageUrl, Collectors.toList())
            ));
    }

    private BooleanExpression shopIdEq(Long shopId) {
        return shopId != null ? shopNoticeJpaEntity.shopId.eq(shopId) : null;
    }

    private BooleanExpression shopNameContains(String shopName) {
        return StringUtils.hasText(shopName) ? shopJpaEntity.name.containsIgnoreCase(shopName) : null;
    }

    private BooleanExpression hiddenEq(Boolean hidden) {
        return hidden != null ? shopNoticeJpaEntity.hidden.eq(hidden) : null;
    }
}
