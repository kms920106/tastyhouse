package com.tastyhouse.core.domain.banner.infrastructure.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.repository.BannerRepository;
import com.tastyhouse.core.domain.banner.domain.vo.BannerId;
import com.tastyhouse.core.domain.banner.application.dto.BannerAdminListItemDto;
import com.tastyhouse.core.domain.banner.application.dto.BannerAdminSearchCondition;
import com.tastyhouse.core.domain.banner.application.dto.BannerListItemDto;
import com.tastyhouse.core.domain.banner.application.dto.QBannerAdminListItemDto;
import com.tastyhouse.core.domain.banner.application.dto.QBannerListItemDto;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.core.domain.banner.domain.model.QBanner.banner;
import static com.tastyhouse.core.domain.file.domain.model.QUploadedFile.uploadedFile;

@Repository
@RequiredArgsConstructor
public class BannerRepositoryImpl implements BannerRepository {

    private final JPAQueryFactory queryFactory;
    private final BannerJpaRepository bannerJpaRepository;

    @Override
    public PageResult<BannerListItemDto> findAllByType(BannerType type, PageQuery pageQuery) {
        LocalDateTime now = LocalDateTime.now();

        Long total = queryFactory
            .select(banner.id.count())
            .from(banner)
            .where(
                banner.type.eq(type),
                banner.deleted.isFalse(),
                banner.visible.isTrue(),
                banner.startDate.loe(now),
                banner.endDate.goe(now)
            )
            .fetchOne();

        List<BannerListItemDto> banners = queryFactory
            .select(new QBannerListItemDto(
                banner.id,
                banner.title,
                uploadedFile.filePath,
                banner.linkUrl
            ))
            .from(banner)
            .join(uploadedFile).on(uploadedFile.id.eq(banner.imageFileId))
            .where(
                banner.type.eq(type),
                banner.deleted.isFalse(),
                banner.visible.isTrue(),
                banner.startDate.loe(now),
                banner.endDate.goe(now)
            )
            .orderBy(banner.sort.asc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(banners, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public PageResult<BannerAdminListItemDto> findAllForAdmin(BannerAdminSearchCondition condition, PageQuery pageQuery) {
        Long total = queryFactory
            .select(banner.id.count())
            .from(banner)
            .where(
                banner.deleted.isFalse(),
                typeEq(condition.type()),
                titleContains(condition.title()),
                visibleEq(condition.visible())
            )
            .fetchOne();

        List<BannerAdminListItemDto> banners = queryFactory
            .select(new QBannerAdminListItemDto(
                banner.id,
                banner.type,
                banner.title,
                uploadedFile.id,
                uploadedFile.originalFilename,
                uploadedFile.filePath,
                banner.linkUrl,
                banner.startDate,
                banner.endDate,
                banner.sort,
                banner.visible
            ))
            .from(banner)
            .leftJoin(uploadedFile).on(uploadedFile.id.eq(banner.imageFileId))
            .where(
                banner.deleted.isFalse(),
                typeEq(condition.type()),
                titleContains(condition.title()),
                visibleEq(condition.visible())
            )
            .orderBy(banner.sort.asc())
            .offset((long) pageQuery.page() * pageQuery.size())
            .limit(pageQuery.size())
            .fetch();

        return PageResult.of(banners, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<Banner> findById(BannerId id) {
        if (id == null) {
            return Optional.empty();
        }
        return bannerJpaRepository.findByIdAndDeletedFalse(id.value());
    }

    @Override
    public Banner save(Banner entity) {
        return bannerJpaRepository.save(entity);
    }

    private BooleanExpression typeEq(BannerType type) {
        return type != null ? banner.type.eq(type) : null;
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? banner.title.containsIgnoreCase(title) : null;
    }

    private BooleanExpression visibleEq(Boolean visible) {
        return visible != null ? banner.visible.eq(visible) : null;
    }
}
