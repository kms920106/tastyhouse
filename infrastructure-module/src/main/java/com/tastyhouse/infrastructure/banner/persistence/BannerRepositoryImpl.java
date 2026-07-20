package com.tastyhouse.infrastructure.banner.persistence;

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
import com.tastyhouse.core.domain.banner.application.dto.BannerSearchCondition;
import com.tastyhouse.core.domain.banner.application.dto.result.BannerListItemResult;
import com.tastyhouse.core.domain.banner.application.dto.result.BannerManagementListItemResult;
import com.tastyhouse.core.domain.banner.application.dto.result.QBannerListItemResult;
import com.tastyhouse.core.domain.banner.application.dto.result.QBannerManagementListItemResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

import static com.tastyhouse.infrastructure.banner.persistence.QBannerJpaEntity.bannerJpaEntity;
import static com.tastyhouse.infrastructure.file.persistence.QUploadedFileJpaEntity.uploadedFileJpaEntity;

@Repository
@RequiredArgsConstructor
public class BannerRepositoryImpl implements BannerRepository {

    private final JPAQueryFactory queryFactory;
    private final BannerJpaRepository bannerJpaRepository;

    @Override
    public PageResult<BannerListItemResult> findAllByType(BannerType type, PageQuery pageQuery) {
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
            .select(new QBannerListItemResult(
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
            .fetch();

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
            .select(new QBannerManagementListItemResult(
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
            .fetch();

        return PageResult.of(banners, total != null ? total : 0L, pageQuery.page(), pageQuery.size());
    }

    @Override
    public Optional<Banner> findById(BannerId id) {
        if (id == null) {
            return Optional.empty();
        }
        return bannerJpaRepository.findByIdAndDeletedFalse(id.value())
            .map(BannerMapper::toDomain);
    }

    @Override
    public Banner save(Banner banner) {
        if (banner.getId() == null) {
            BannerJpaEntity saved = bannerJpaRepository.save(BannerMapper.toEntity(banner));
            return BannerMapper.toDomain(saved);
        }

        // update 경로: managed 엔티티를 PK로 조회(동일 트랜잭션이면 1차 캐시 히트)한 뒤 변경 필드만 복사해
        // dirty checking으로 flush. detached merge는 @CreatedDate(updatable=false) 감사 필드 파손 위험이 있어 쓰지 않는다.
        BannerJpaEntity entity = bannerJpaRepository.findById(banner.getId())
            .orElseThrow(() -> new IllegalStateException("존재하지 않는 배너입니다: " + banner.getId()));
        BannerMapper.applyChanges(entity, banner);
        return BannerMapper.toDomain(entity);
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
