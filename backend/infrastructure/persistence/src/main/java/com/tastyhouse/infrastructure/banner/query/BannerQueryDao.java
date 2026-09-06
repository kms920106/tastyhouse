package com.tastyhouse.infrastructure.banner.query;

import com.tastyhouse.application.banner.port.out.BannerManagementQueryPort;
import com.tastyhouse.application.banner.port.out.BannerQueryPort;
import com.tastyhouse.application.banner.port.out.BannerDetailResult;
import com.tastyhouse.application.banner.port.out.BannerListItemResult;
import com.tastyhouse.application.banner.port.out.BannerManagementListItemResult;
import com.tastyhouse.application.banner.port.out.BannerSearchCondition;
import com.querydsl.core.types.Projections;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.domain.banner.model.BannerType;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.file.query.FileUrlResolver;

import static com.tastyhouse.infrastructure.banner.persistence.QBannerJpaEntity.bannerJpaEntity;
import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;

@Repository
public class BannerQueryDao implements BannerQueryPort, BannerManagementQueryPort {
    private final JPAQueryFactory queryFactory;
    private final FileUrlResolver fileUrlResolver;

    public BannerQueryDao(JPAQueryFactory queryFactory, FileUrlResolver fileUrlResolver) {
        this.queryFactory = queryFactory;
        this.fileUrlResolver = fileUrlResolver;
    }

    @Override
    public PageResult<BannerListItemResult> findVisibleBannersByType(BannerType type, PageQuery pageQuery) {
        LocalDateTime now = LocalDateTime.now();

        Long total = queryFactory
            .select(bannerJpaEntity.id.count())
            .from(bannerJpaEntity)
            .where(
                bannerJpaEntity.type.eq(type),
                bannerJpaEntity.deleted.isFalse(),
                bannerJpaEntity.visible.isTrue(),
                bannerJpaEntity.startDate.loe(now),
                bannerJpaEntity.endDate.goe(now)
            )
            .fetchOne();

        List<BannerListItemResult> banners = queryFactory
            .select(Projections.constructor(BannerListItemResult.class,
                bannerJpaEntity.id,
                bannerJpaEntity.title,
                uploadedFileJpaEntity.filePath,
                bannerJpaEntity.linkUrl
            ))
            .from(bannerJpaEntity)
            .join(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(bannerJpaEntity.imageFileId))
            .where(
                bannerJpaEntity.type.eq(type),
                bannerJpaEntity.deleted.isFalse(),
                bannerJpaEntity.visible.isTrue(),
                bannerJpaEntity.startDate.loe(now),
                bannerJpaEntity.endDate.goe(now)
            )
            .orderBy(bannerJpaEntity.sort.asc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(banners, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<BannerManagementListItemResult> findAllBanners(BannerSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(bannerJpaEntity.id.count())
            .from(bannerJpaEntity)
            .where(
                bannerJpaEntity.deleted.isFalse(),
                typeEq(condition.type()),
                titleContains(condition.title()),
                visibleEq(condition.visible())
            )
            .fetchOne();

        List<BannerManagementListItemResult> banners = queryFactory
            .select(Projections.constructor(BannerManagementListItemResult.class,
                bannerJpaEntity.id,
                bannerJpaEntity.type,
                bannerJpaEntity.title,
                uploadedFileJpaEntity.id,
                uploadedFileJpaEntity.originalFilename,
                uploadedFileJpaEntity.filePath,
                bannerJpaEntity.linkUrl,
                bannerJpaEntity.startDate,
                bannerJpaEntity.endDate,
                bannerJpaEntity.sort,
                bannerJpaEntity.visible
            ))
            .from(bannerJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(bannerJpaEntity.imageFileId))
            .where(
                bannerJpaEntity.deleted.isFalse(),
                typeEq(condition.type()),
                titleContains(condition.title()),
                visibleEq(condition.visible())
            )
            .orderBy(bannerJpaEntity.sort.asc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch()
            .stream()
            .map(this::withResolvedImageUrl)
            .toList();

        return PageResult.of(banners, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<BannerDetailResult> findDetailById(Long id) {
        if (id == null) {
            return Optional.empty();
        }

        BannerDetailResult detail = queryFactory
            .select(Projections.constructor(BannerDetailResult.class,
                bannerJpaEntity.id,
                bannerJpaEntity.type,
                bannerJpaEntity.title,
                uploadedFileJpaEntity.id,
                uploadedFileJpaEntity.originalFilename,
                uploadedFileJpaEntity.filePath,
                bannerJpaEntity.linkUrl,
                bannerJpaEntity.startDate,
                bannerJpaEntity.endDate,
                bannerJpaEntity.sort,
                bannerJpaEntity.visible,
                bannerJpaEntity.createdAt,
                bannerJpaEntity.updatedAt
            ))
            .from(bannerJpaEntity)
            .leftJoin(uploadedFileJpaEntity).on(uploadedFileJpaEntity.id.eq(bannerJpaEntity.imageFileId))
            .where(bannerJpaEntity.id.eq(id), bannerJpaEntity.deleted.isFalse())
            .fetchOne();

        return Optional.ofNullable(detail).map(this::withResolvedImageUrl);
    }

    private BannerListItemResult withResolvedImageUrl(BannerListItemResult row) {
        return new BannerListItemResult(
            row.id(),
            row.title(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.linkUrl()
        );
    }

    private BannerManagementListItemResult withResolvedImageUrl(BannerManagementListItemResult row) {
        return new BannerManagementListItemResult(
            row.id(),
            row.type(),
            row.title(),
            row.imageFileId(),
            row.imageFileName(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.linkUrl(),
            row.startDate(),
            row.endDate(),
            row.sort(),
            row.visible()
        );
    }

    private BannerDetailResult withResolvedImageUrl(BannerDetailResult row) {
        return new BannerDetailResult(
            row.id(),
            row.type(),
            row.title(),
            row.imageFileId(),
            row.imageFileName(),
            fileUrlResolver.resolve(row.imageUrl()),
            row.linkUrl(),
            row.startDate(),
            row.endDate(),
            row.sort(),
            row.visible(),
            row.createdAt(),
            row.updatedAt()
        );
    }

    private BooleanExpression typeEq(BannerType type) {
        return type != null ? bannerJpaEntity.type.eq(type) : null;
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? bannerJpaEntity.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? bannerJpaEntity.visible.eq(visible) : null;
    }
}
