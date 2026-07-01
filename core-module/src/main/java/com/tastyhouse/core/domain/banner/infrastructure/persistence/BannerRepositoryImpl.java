package com.tastyhouse.core.domain.banner.infrastructure.persistence;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.tastyhouse.core.domain.banner.application.dto.BannerListItemDto;
import com.tastyhouse.core.domain.banner.application.dto.QBannerListItemDto;
import com.tastyhouse.core.domain.banner.domain.model.Banner;
import com.tastyhouse.core.domain.banner.domain.model.BannerType;
import com.tastyhouse.core.domain.banner.domain.repository.BannerRepository;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

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
    public Optional<Banner> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return bannerJpaRepository.findById(id);
    }
}
