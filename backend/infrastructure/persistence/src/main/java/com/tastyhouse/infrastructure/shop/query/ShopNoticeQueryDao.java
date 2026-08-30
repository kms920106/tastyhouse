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

/**
 * 점주 공지 read 어댑터(CQRS query 측).
 *
 * <p>공지 본문과 첨부 이미지를 <b>두 쿼리로 나눠</b> 읽고 {@code shopNoticeId}로 묶는다 — 1:N 조인으로
 * 한 번에 읽으면 공지 행이 이미지 수만큼 중복되어 페이징 카운트가 어긋난다. 이미지 URL은
 * {@link FileUrlResolver}로 조회 시점에 완성하므로 소비 Service는 파일 식별자를 보지 않는다.
 *
 * <p>{@code shop} 도메인은 대형이라 용도별 DAO 분리가 허용된다 — 공지는 본문·이미지 2단 조회라는 고유한
 * 조립 형태를 가지므로 {@code ShopQueryDao}에 섞지 않고 별도 DAO로 둔다.
 */
@Repository
public class ShopNoticeQueryDao implements ShopNoticeQueryPort, ShopNoticeOwnerQueryPort, ShopNoticeManagementQueryPort {

    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public ShopNoticeQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    /**
     * 가게의 공지 목록(점주 화면) — 노출중 공지를 맨 위로, 그다음 최근 등록 순.
     */
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

    /**
     * 가게에서 현재 앱에 노출 중인 공지(web 화면) — {@code exposed = true AND hidden = false} 최대 1건.
     */
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

    /**
     * 공지 목록 페이징(관리 화면) — 가게·가게명·게시중단 여부로 필터하며, 최근 등록 순.
     */
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

    /**
     * 공지 ID 묶음의 첨부 이미지를 한 쿼리로 읽어 공지별 URL 목록으로 묶는다({@code sortOrder} 오름차순).
     */
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
